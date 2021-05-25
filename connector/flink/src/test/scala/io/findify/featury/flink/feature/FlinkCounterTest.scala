package io.findify.featury.flink.feature

import io.findify.featury.flink.FlinkStreamTest
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.{CounterValue, FeatureKey, Key, Timestamp, Write}
import io.findify.featury.model.Key.{FeatureName, GroupName, Id, Namespace, Tenant}
import io.findify.featury.model.Write.Increment
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.flink.api.scala._

import java.time.Duration
import scala.concurrent.duration._

class FlinkCounterTest extends AnyFlatSpec with FlinkStreamTest with Matchers {
  import io.findify.featury.flink.FeaturyFlow._

  val now  = Timestamp.now
  val k    = Key(Namespace("dev"), GroupName("product"), FeatureName("clicks"), Tenant(1), Id("x1"))
  val fkey = FeatureKey(k.ns, k.group, k.name)

//  it should "process increments" in {
//    val conf   = Map(fkey -> CounterConfig(fkey.feature, fkey.ns, fkey.group, refresh = 0.seconds))
//    val result = writeIncrements(conf, List(Increment(k, now, 1), Increment(k, now.plus(1.seconds), 1)))
//    result.map(_.value.value) shouldBe List(1, 2)
//  }

  it should "process increments with refresh" in {
    val conf   = Map(fkey -> CounterConfig(fkey.feature, fkey.ns, fkey.group, refresh = 1.hour))
    val list   = (0 until 300).map(i => Increment(k, now.plus(i.minutes), 1)).toList
    val result = writeIncrements(conf, list)
    result.map(_.value) shouldBe List(1, 2)
  }

  def writeIncrements(conf: Map[FeatureKey, CounterConfig], values: List[Increment]): List[CounterValue] = {
    env
      .fromCollection[Write](values)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forBoundedOutOfOrderness[Write](java.time.Duration.ofSeconds(1))
          .withTimestampAssigner(new SerializableTimestampAssigner[Write] {
            override def extractTimestamp(element: Write, recordTimestamp: Long): Long = element.ts.ts
          })
      )
      .processCounters(conf)
      .executeAndCollect(100)
  }

}
