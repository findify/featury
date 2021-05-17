package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.feature.Feature.State
import io.findify.featury.model.Key
import io.findify.featury.model.FeatureValue.{Num, NumScalarValue}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.FeatureConfig

trait Counter extends Feature[CounterState, NumScalarValue, CounterConfig] {
  def config: CounterConfig
  def increment(key: Key, value: Long): IO[Unit]
  override def empty(): CounterState                                     = CounterState(0)
  override def computeValue(state: CounterState): Option[NumScalarValue] = Some(NumScalarValue(Num(state.value)))
}

object Counter {
  case class CounterState(value: Long) extends State {
    def increment(inc: Long) = CounterState(value + inc)
  }
  case class CounterConfig(name: FeatureName, ns: Namespace, group: GroupName) extends FeatureConfig
}
