package io.findify.featury.flink.feature

import io.findify.featury.features.BoundedListSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, ScalarConfig}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.Append
import io.findify.featury.model.{BoundedListValue, FeatureKey, Key, ScalarValue, Write}
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FlinkBoundedListTest extends BoundedListSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  override def write(values: List[Append]): Option[BoundedListValue] = {
    val conf = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 0.hour))
    write(conf, values).lastOption
  }

  def write(conf: Map[FeatureKey, BoundedListConfig], values: List[Append]): List[BoundedListValue] = {
    FeaturyFlow.processList(env.fromCollection[Write](values), conf).executeAndCollect(100)
  }
}
