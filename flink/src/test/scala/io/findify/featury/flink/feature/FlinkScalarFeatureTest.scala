package io.findify.featury.flink.feature

import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
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
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{Append, Put}

import scala.concurrent.duration._
import io.findify.flinkadt.api._

class FlinkScalarFeatureTest extends ScalarFeatureSuite with FlinkStreamTest {
  val k = Key(config.ns, config.scope, config.name, Tenant("1"), Id("x1"))

  override def write(values: List[Put]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    FeaturyFlow.process(env.fromCollection[Write](values), conf).executeAndCollect(100).lastOption
  }

}
