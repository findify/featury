package io.findify.featury.flink.feature

import io.findify.featury.features.PeriodicCounterSuite
import io.findify.featury.flink.{FeaturyFlow, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, PeriodicCounterConfig}
import io.findify.featury.model.{BoundedListValue, FeatureKey, Key, PeriodicCounterState, PeriodicCounterValue, Write}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{Append, PeriodicIncrement}
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FlinkPeriodicCounterTest extends PeriodicCounterSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant(1), Id("x1"))

  override def write(values: List[PeriodicIncrement]): Option[PeriodicCounterValue] = {
    val conf = Map(FeatureKey(k.ns, k.group, k.name) -> config.copy(refresh = 0.hour))
    write(conf, values).lastOption
  }

  def write(
      conf: Map[FeatureKey, PeriodicCounterConfig],
      values: List[PeriodicIncrement]
  ): List[PeriodicCounterValue] = {
    FeaturyFlow.processPeriodicCounters(env.fromCollection[Write](values), conf).executeAndCollect(100)
  }

}
