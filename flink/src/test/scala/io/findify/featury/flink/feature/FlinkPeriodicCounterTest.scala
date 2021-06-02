package io.findify.featury.flink.feature

import io.findify.featury.features.PeriodicCounterSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, PeriodicCounterConfig}
import io.findify.featury.model.{
  BoundedListValue,
  FeatureKey,
  FeatureValue,
  Key,
  PeriodicCounterState,
  PeriodicCounterValue,
  Schema,
  Write
}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{Append, PeriodicIncrement}
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FlinkPeriodicCounterTest extends PeriodicCounterSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  override def write(values: List[PeriodicIncrement]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    FeaturyFlow.process(env.fromCollection[Write](values), conf).executeAndCollect(100).lastOption
  }

}
