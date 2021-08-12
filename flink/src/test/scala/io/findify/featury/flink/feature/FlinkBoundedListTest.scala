package io.findify.featury.flink.feature

import io.findify.featury.features.BoundedListSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, ScalarConfig}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.Append
import io.findify.featury.model.{
  BoundedListValue,
  FeatureConfig,
  FeatureKey,
  FeatureValue,
  Key,
  ScalarValue,
  Schema,
  Write
}
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FlinkBoundedListTest extends BoundedListSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant("1"), Id("x1"))

  override def write(values: List[Append]): Option[FeatureValue] = {
    val conf = Schema(List(config.copy(refresh = 0.hour)))
    Featury
      .process(env.fromCollection[Write](values), conf)
      .executeAndCollect(100)
      .lastOption
  }

}
