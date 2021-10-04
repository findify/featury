package io.findify.featury.flink.feature

import io.findify.featury.features.FreqEstimatorSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model.Key.{Tag, Tenant}
import io.findify.featury.model.Write.{PeriodicIncrement, PutFreqSample}
import io.findify.featury.model.{FeatureKey, FeatureValue, FrequencyValue, Key, PeriodicCounterValue, Schema, Write}
import io.findify.flinkadt.api._

import scala.concurrent.duration._

class FlinkFreqEstimatorTest extends FreqEstimatorSuite with FlinkStreamTest {
  val k = Key(Tag(config.scope, "x1"), config.name, Tenant("1"))

  override def write(values: List[PutFreqSample]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100).lastOption
  }

}
