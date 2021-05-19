package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.feature.Feature.State
import io.findify.featury.model.Key
import io.findify.featury.model.FeatureValue.{Num, ScalarValue}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.FeatureConfig

trait Counter extends Feature[CounterState, ScalarValue[Num], CounterConfig] {
  def increment(key: Key, value: Long): IO[Unit]
  override def computeValue(state: CounterState): Option[ScalarValue[Num]] = Some(ScalarValue(Num(state.value)))
}

object Counter {
  case class CounterState(value: Long) extends State {
    def increment(inc: Long) = CounterState(value + inc)
  }
  case class CounterConfig(name: FeatureName, ns: Namespace, group: GroupName) extends FeatureConfig
}
