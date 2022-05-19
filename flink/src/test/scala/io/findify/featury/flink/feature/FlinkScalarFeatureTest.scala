package io.findify.featury.flink.feature

import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest, TypeInfoCache}
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, CounterConfig, ScalarConfig}
import io.findify.featury.model.{
  BoundedListValue,
  CounterValue,
  FeatureKey,
  FeatureValue,
  Key,
  ScalarValue,
  Schema,
  Write
}
import io.findify.featury.model.Key.{Tag, Tenant}
import io.findify.featury.model.Write.{Append, Put}

import scala.concurrent.duration._
import io.findify.flinkadt.api._

import scala.language.higherKinds

class FlinkScalarFeatureTest extends ScalarFeatureSuite with FlinkStreamTest {
  import TypeInfoCache._
  val k = Key(Tag(config.scope, "x1"), config.name, Tenant("1"))

  override def write(values: List[Put]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100).lastOption
  }

}
