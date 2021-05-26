package io.findify.featury.flink.feature

import io.findify.featury.features.FreqEstimatorSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{FreqEstimatorConfig, PeriodicCounterConfig}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{PeriodicIncrement, PutFreqSample}
import io.findify.featury.model.{FeatureKey, FrequencyValue, Key, PeriodicCounterValue, Write}
import org.apache.flink.api.scala._
import scala.concurrent.duration._

class FlinkFreqEstimatorTest extends FreqEstimatorSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  override def write(values: List[PutFreqSample]): Option[FrequencyValue] = {
    val conf = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 0.hour))
    write(conf, values).lastOption
  }

  def write(
      conf: Map[FeatureKey, FreqEstimatorConfig],
      values: List[PutFreqSample]
  ): List[FrequencyValue] = {
    FeaturyFlow.processFreqEstimators(env.fromCollection[Write](values), conf).executeAndCollect(100)
  }

}
