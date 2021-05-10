package io.findify.featury.feature

import io.findify.featury.model.{Feature, Timestamp}
import io.findify.featury.model.Feature.{Op, State}
import io.findify.featury.model.FeatureValue.{Num, ScalarValue}
import io.findify.featury.model.Schema.FeatureConfig

object Counter {
  case class Increment(value: Double, ts: Timestamp) extends Op
  case class CounterState(value: Double)             extends State
  case class CounterConfig()                         extends FeatureConfig

  object CounterFeature extends Feature[CounterState, ScalarValue[Num], Increment, CounterConfig] {

    override def emptyState(conf: CounterConfig): CounterState   = CounterState(0.0)
    override def value(conf: CounterConfig, state: CounterState) = ScalarValue(Num(state.value))

    override def update(conf: CounterConfig, state: CounterState, op: Increment): CounterState = CounterState(
      state.value + op.value
    )
  }
}
