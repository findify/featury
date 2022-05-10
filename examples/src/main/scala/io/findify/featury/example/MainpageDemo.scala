package io.findify.featury.example

import io.findify.featury.flink.{Featury, Join}
import io.findify.featury.model.FeatureConfig.{
  CounterConfig,
  PeriodRange,
  PeriodicCounterConfig,
  ScalarConfig,
  StatsEstimatorConfig
}
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.Write.{Increment, PeriodicIncrement, Put, PutStatSample}
import io.findify.featury.model.{FeatureValue, Key, SDouble, SString, Schema, Timestamp, Write}
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import io.findify.flink.api._
import io.findify.flinkadt.api._
import scala.concurrent.duration._
import scala.language.higherKinds

object MainpageDemo {
  // our original business events
  sealed trait Event {
    def ts: Timestamp
  }
  // items description, title and inventory count
  case class Metadata(id: String, title: String, count: Int, ts: Timestamp) extends Event
  // item was shown to the user
  case class Impression(ids: List[String], user: String, ts: Timestamp) extends Event
  // when user interacted with item `id`
  case class Click(id: String, user: String, ts: Timestamp) extends Event

  case class Clickthrough(
      user: String,
      impressions: List[String],
      clicks: List[String],
      ts: Timestamp,
      values: List[FeatureValue] = Nil
  )

  // a set of helper values
  val now    = Timestamp.now
  val tenant = Tenant("1")

  // on which scopes we operate
  val MerchantScope = Scope("merchant")
  val ProductScope  = Scope("product")
  val UserScope     = Scope("user")

  // a schema definition, describing our ML features

  // store per-product title updates
  val productTitle = ScalarConfig(ProductScope, FeatureName("title"), refresh = 0.seconds)
  // store per-product inventory counts
  val productCount = ScalarConfig(ProductScope, FeatureName("count"), refresh = 0.seconds)
  // numerical statistics for product counts
  val countStats = StatsEstimatorConfig(
    MerchantScope,
    FeatureName("count_stats"),
    refresh = 0.seconds,
    poolSize = 100,                    // sample buffer for 100 entries
    sampleRate = 1.0,                  // sample each item
    percentiles = List(30, 50, 70, 90) // emit these percentiles
  )
  // aggregated counters of clicks for last 1, 12, 24 hours
  val productClicks = PeriodicCounterConfig(
    ProductScope,
    FeatureName("click_count"),
    refresh = 0.seconds,
    period = 1.hour,                                                                  // bucket size
    sumPeriodRanges = List(PeriodRange(1, 0), PeriodRange(12, 0), PeriodRange(24, 0)) // periods are in buckets
  )
  // aggregated counters of impressions for last 1, 12, 24 hours
  val productImpressions = PeriodicCounterConfig(
    ProductScope,
    FeatureName("impress_count"),
    refresh = 0.seconds,
    period = 1.hour,                                                                  // bucket size
    sumPeriodRanges = List(PeriodRange(1, 0), PeriodRange(12, 0), PeriodRange(24, 0)) // periods are in buckets
  )
  // number of clicks made by the user
  val userClicks = CounterConfig(UserScope, FeatureName("click_count"), refresh = 0.seconds)

  val schema = Schema(List(productTitle, productCount, countStats, productClicks, productImpressions, userClicks))

  def main(args: Array[String]): Unit = {
    // scala flink env to run all the tasks
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setRuntimeMode(RuntimeExecutionMode.BATCH)

    val events = env
      .fromCollection[Event](
        List(
          Impression(List("p1", "p2", "p3"), "u0", now),
          Click("p2", "u0", now.plus(1.second)),
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
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forMonotonousTimestamps()
          .withTimestampAssigner(new SerializableTimestampAssigner[Event] {
            override def extractTimestamp(element: Event, recordTimestamp: Long): Long = element.ts.ts
          })
      )

    // now we need to convert our business events into Write actions
    val writes: DataStream[Write] = events.flatMap(_ match {
      case Metadata(id, title, count, ts) =>
        List(
          Put(Key(productTitle, tenant, id), ts, SString(title)),         // put title
          Put(Key(productCount, tenant, id), ts, SDouble(count)),         // put count
          PutStatSample(Key(countStats, tenant, tenant.value), ts, count) // sample count
        )
      case Impression(ids, user, ts) =>
        ids.map(id => PeriodicIncrement(Key(productImpressions, tenant, id), ts, 1)) // count per product impressions
      case Click(id, user, ts) =>
        List(
          PeriodicIncrement(Key(productClicks, tenant, id), ts, 1), // per-product clicks
          Increment(Key(userClicks, tenant, user), ts, 1)           // per-user clicks
        )
    })

    // here we pipe writes through schema and compute feature update changelog
    val features       = Featury.process(writes, schema, 10.seconds)
    val featuresList   = features.executeAndCollect().toList
    val breakpointHere = 0

    // an example clickthrough session
    val sessions = env
      .fromCollection(
        List(
          Clickthrough("u1", List("p1", "p2", "p3"), List("p2", "p3"), now.plus(3.minute))
        )
      )
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forMonotonousTimestamps()
          .withTimestampAssigner(new SerializableTimestampAssigner[Clickthrough] {
            override def extractTimestamp(element: Clickthrough, recordTimestamp: Long): Long = element.ts.ts
          })
      )
    val join = new Join[Clickthrough] {
      override def by(left: Clickthrough): Tenant = tenant

      // by which fields to we want to make a join?
      override def tags(left: Clickthrough): List[Key.Tag] = {
        List(
          Tag(MerchantScope, "1"),                           // so join all merchant-wide features
          Tag(UserScope, left.user)                          // also user-specific ones
        ) ++ left.impressions.map(p => Tag(ProductScope, p)) // and also per-product ones
      }

      // how to merge our custom Clickthrough and FeatureValues
      override def join(left: Clickthrough, values: List[FeatureValue]): Clickthrough =
        left.copy(values = left.values ++ values)
    }
    // here we see all the joined feature values for each tag
    val joined          = Featury.join(features, sessions, join, schema)
    val joinedValues    = joined.executeAndCollect().toList
    val breakpointHere2 = 1
  }
}
