package io.findify.featury.flink.feature

import io.findify.featury.features.StatsEstimatorSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{FreqEstimatorConfig, StatsEstimatorConfig}
import io.findify.featury.model.{FeatureKey, FrequencyValue, Key, NumStatsValue, Write}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{PutFreqSample, PutStatSample}
import org.apache.flink.api.scala._
import scala.concurrent.duration._

class FlinkStatsEstimatorTest extends StatsEstimatorSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  override def write(values: List[PutStatSample]): Option[NumStatsValue] = {
    val conf = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 0.hour))
    write(conf, values).lastOption
  }

  def write(
      conf: Map[FeatureKey, StatsEstimatorConfig],
      values: List[PutStatSample]
  ): List[NumStatsValue] = {
    FeaturyFlow.processStatEstimators(env.fromCollection[Write](values), conf).executeAndCollect(100)
  }

}
