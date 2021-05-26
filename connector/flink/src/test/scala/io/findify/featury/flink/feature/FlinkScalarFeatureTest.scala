package io.findify.featury.flink.feature

import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, CounterConfig, ScalarConfig}
import io.findify.featury.model.{BoundedListValue, CounterValue, FeatureKey, Key, ScalarValue, Write}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{Append, Put}

import scala.concurrent.duration._
import org.apache.flink.api.scala._

class FlinkScalarFeatureTest extends ScalarFeatureSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  override def write(values: List[Put]): Option[ScalarValue] = {
    val conf = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 0.hour))
    write(conf, values).lastOption
  }

  def write(conf: Map[FeatureKey, ScalarConfig], values: List[Write]): List[ScalarValue] = {
    FeaturyFlow.processScalar(env.fromCollection[Write](values), conf).executeAndCollect(100)
  }

}
