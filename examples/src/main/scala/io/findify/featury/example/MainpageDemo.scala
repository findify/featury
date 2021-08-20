package io.findify.featury.example

import io.findify.featury.example.MainpageDemo.Click
import io.findify.featury.flink.Featury
import io.findify.featury.model.FeatureConfig.{
  CounterConfig,
  PeriodRange,
  PeriodicCounterConfig,
  ScalarConfig,
  StatsEstimatorConfig
}
import io.findify.featury.model.Key.{FeatureName, Id, Namespace, Scope, Tenant}
import io.findify.featury.model.Write.{Increment, PeriodicIncrement, Put, PutStatSample}
import io.findify.featury.model.{Key, SDouble, SString, Schema, Timestamp, Write}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.api.scala.extensions._

import scala.concurrent.duration._

object MainpageDemo {
  // our original business events
  sealed trait Event
  // items description, title and inventory count
  case class Metadata(id: String, title: String, count: Int, ts: Timestamp) extends Event
  // item was shown to the user
  case class Impression(ids: List[String], user: String, ts: Timestamp) extends Event
  // when user interacted with item `id`
  case class Click(id: String, user: String, ts: Timestamp) extends Event


  case class Clickthrough(user: String,)

  // a set of helper values
  val now    = Timestamp.now
  val ns     = Namespace("dev")
  val tenant = Tenant("1")

  // on which scopes we operate
  val MerchantScope = Scope("merchant")
  val ProductScope  = Scope("product")
  val UserScope     = Scope("user")

  // a schema definition, describing our ML features

  // store per-product title updates
  val productTitle = ScalarConfig(ns, ProductScope, FeatureName("title"), refresh = 0.seconds)
  // store per-product inventory counts
  val productCount = ScalarConfig(ns, ProductScope, FeatureName("count"), refresh = 0.seconds)
  // numerical statistics for product counts
  val countStats = StatsEstimatorConfig(
    ns,
    MerchantScope,
    FeatureName("count_stats"),
    refresh = 0.seconds,
    poolSize = 100,                    // sample buffer for 100 entries
    sampleRate = 1.0,                  // sample each item
    percentiles = List(30, 50, 70, 90) // emit these percentiles
  )
  // aggregated counters of clicks for last 1, 12, 24 hours
  val productClicks = PeriodicCounterConfig(
    ns,
    ProductScope,
    FeatureName("click_count"),
    refresh = 0.seconds,
    period = 1.hour,                                                                  // bucket size
    sumPeriodRanges = List(PeriodRange(1, 0), PeriodRange(12, 0), PeriodRange(24, 0)) // periods are in buckets
  )
  // aggregated counters of impressions for last 1, 12, 24 hours
  val productImpressions = PeriodicCounterConfig(
    ns,
    ProductScope,
    FeatureName("impress_count"),
    refresh = 0.seconds,
    period = 1.hour,                                                                  // bucket size
    sumPeriodRanges = List(PeriodRange(1, 0), PeriodRange(12, 0), PeriodRange(24, 0)) // periods are in buckets
  )
  // number of clicks made by the user
  val userClicks = CounterConfig(ns, UserScope, FeatureName("click_count"), refresh = 0.seconds)

  val schema = Schema(List(productTitle, productCount, countStats, productClicks, productImpressions, userClicks))

  def main(args: Array[String]): Unit = {
    // scala flink env to run all the tasks
    val env = StreamExecutionEnvironment.createLocalEnvironment(1)

    val events = env.fromCollection(
      List(
        // some product metadata at first
        Metadata("p1", "socks", 10, now),
        Metadata("p2", "pants", 100, now.plus(1.minute)),
        Metadata("p3", "t-shirt", 50, now.plus(2.minute)),
        // then some interactions
        Impression(List("p1", "p2", "p3"), "u1", now.plus(3.minute)),
        Click("p2", "u1", now.plus(4.minute)),
        Metadata("p3", "t-shirt", 49, now.plus(5.minute)), // someone bought one, so count was decremented
        Click("p3", "u1", now.plus(6.minute))
      )
    )

    // now we need to convert our business events into Write actions
    val writes: DataStream[Write] = events.flatMap(_ match {
      case Metadata(id, title, price, ts) =>
        List(
          Put(Key(productTitle, tenant, Id(id)), ts, SString(title)),         // put title
          Put(Key(productCount, tenant, Id(id)), ts, SDouble(price)),         // put count
          PutStatSample(Key(countStats, tenant, Id(tenant.value)), ts, price) // sample count
        )
      case Impression(ids, user, ts) =>
        ids.map(id =>
          PeriodicIncrement(Key(productImpressions, tenant, Id(id)), ts, 1)
        ) // count per product impressions
      case Click(id, user, ts) =>
        List(
          PeriodicIncrement(Key(productClicks, tenant, Id(id)), ts, 1), // per-product clicks
          Increment(Key(userClicks, tenant, Id(user)), ts, 1)           // per-user clicks
        )
    })

    // here we pipe writes through schema and compute feature update changelog
    val features = Featury.process(writes, schema, 10.seconds)

  }
}
