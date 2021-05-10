package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.feature.Feature.State
import io.findify.featury.model.Key
import io.findify.featury.model.FeatureValue.{Num, ScalarValue}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.Schema.FeatureConfig

trait Counter extends Feature[CounterState, ScalarValue[Num]] {
  def config: CounterConfig
  def increment(key: Key, value: Double): IO[Unit]
  override def empty(): CounterState                               = CounterState(0)
  override def computeValue(state: CounterState): ScalarValue[Num] = ScalarValue(Num(state.value))
}

object Counter {
  case class CounterState(value: Double) extends State {
    def increment(inc: Double) = CounterState(value + inc)
  }
  case class CounterConfig(name: FeatureName) extends FeatureConfig

}
