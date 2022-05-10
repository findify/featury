package io.findify.featury.flink.feature

import io.findify.featury.features.CounterSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.{CounterValue, FeatureKey, FeatureValue, Key, SString, Schema, Timestamp, Write}
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.Write.{Append, Increment}
import io.findify.flinkadt.api._

import scala.concurrent.duration._
import scala.language.higherKinds

class FlinkCounterTest extends CounterSuite with FlinkStreamTest {

  val k = Key(Tag(config.scope, "x1"), config.name, Tenant("1"))

  it should "process increments with refresh" in {
    val conf   = Schema(config.copy(refresh = 1.hour))
    val list   = (0 until 300).map(i => Increment(k, now.plus(i.minutes), 1)).toList
    val result = writeIncrements(conf, list)
    result
      .collect { case c: CounterValue =>
        c
      }
      .map(_.value) shouldBe List(1, 61, 121, 181, 241)
  }

  it should "handle type mismatch" in {
    val conf   = Schema(config.copy(refresh = 1.hour))
    val values = List(Append(k, SString("fff"), now))
    val result = Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100)
    result shouldBe Nil
  }

  override def write(values: List[Increment]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    writeIncrements(conf, values).lastOption
  }

  def writeIncrements(conf: Schema, values: List[Increment]): List[FeatureValue] = {
    Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100)
  }

}
