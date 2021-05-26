package io.findify.featury.flink.feature

import io.findify.featury.features.CounterSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.{CounterValue, FeatureKey, Key, Timestamp, Write}
import io.findify.featury.model.Key.{FeatureName, GroupName, Id, Namespace, Tenant}
import io.findify.featury.model.Write.Increment
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FlinkCounterTest extends CounterSuite with FlinkStreamTest {

  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  it should "process increments with refresh" in {
    val conf   = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 1.hour))
    val list   = (0 until 300).map(i => Increment(k, now.plus(i.minutes), 1)).toList
    val result = writeIncrements(conf, list)
    result.map(_.value) shouldBe List(1, 61, 121, 181, 241)
  }

  override def write(values: List[Increment]): Option[CounterValue] = {
    val conf = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 0.hour))
    writeIncrements(conf, values).lastOption
  }

  def writeIncrements(conf: Map[FeatureKey, CounterConfig], values: List[Increment]): List[CounterValue] = {
    FeaturyFlow.processCounters(env.fromCollection[Write](values), conf).executeAndCollect(100)
  }

}
