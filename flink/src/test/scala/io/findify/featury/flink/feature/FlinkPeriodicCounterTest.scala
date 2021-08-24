package io.findify.featury.flink.feature

import io.findify.featury.features.PeriodicCounterSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
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
import io.findify.featury.model.Key.{Tag, Tenant}
import io.findify.featury.model.Write.{Append, PeriodicIncrement}
import io.findify.flinkadt.api._

import scala.concurrent.duration._

class FlinkPeriodicCounterTest extends PeriodicCounterSuite with FlinkStreamTest {
  val k = Key(config.ns, Tag(config.scope, "x1"), config.name, Tenant("1"))

  override def write(values: List[PeriodicIncrement]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100).lastOption
  }

}
