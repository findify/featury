package io.findify.featury.flink.feature

import io.findify.featury.features.CounterSuite
import io.findify.featury.flink.FlinkStreamTest
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.{CounterValue, FeatureKey, Key, Timestamp, Write}
import io.findify.featury.model.Key.{FeatureName, GroupName, Id, Namespace, Tenant}
import io.findify.featury.model.Write.Increment
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.scala._
import scala.concurrent.duration._

class FlinkCounterTest extends CounterSuite with FlinkStreamTest {
  import io.findify.featury.flink.FeaturyFlow._

  val k    = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))
  val fkey = FeatureKey(k.ns, k.group, k.name)

  it should "process increments with refresh" in {
    val conf   = Map(fkey -> config.copy(refresh = 1.hour))
    val list   = (0 until 300).map(i => Increment(k, now.plus(i.minutes), 1)).toList
    val result = writeIncrements(conf, list)
    result.map(_.value) shouldBe List(1, 62, 123, 184, 245)
  }

  override def write(values: List[Increment]): Option[CounterValue] = {
    val conf = Map(fkey -> config.copy(refresh = 0.hour))
    writeIncrements(conf, values).lastOption
  }

  def writeIncrements(conf: Map[FeatureKey, CounterConfig], values: List[Increment]): List[CounterValue] = {
    env
      .fromCollection[Write](values)
      .processCounters(conf)
      .executeAndCollect(100)
  }

}
