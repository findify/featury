package io.findify.featury.flink.feature

import io.findify.featury.features.StatsEstimatorSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest, TypeInfoCache}
import io.findify.featury.model.FeatureConfig.{FreqEstimatorConfig, StatsEstimatorConfig}
import io.findify.featury.model.{FeatureKey, FeatureValue, FrequencyValue, Key, NumStatsValue, Schema, Write}
import io.findify.featury.model.Key.{Tag, Tenant}
import io.findify.featury.model.Write.{PutFreqSample, PutStatSample}
import io.findify.flinkadt.api._

import scala.concurrent.duration._
import scala.language.higherKinds

class FlinkStatsEstimatorTest extends StatsEstimatorSuite with FlinkStreamTest {
  import TypeInfoCache._
  val k = Key(Tag(config.scope, "x1"), config.name, Tenant("1"))

  override def write(values: List[PutStatSample]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100).lastOption
  }

}
