package io.findify.featury.model

import io.findify.featury.model.Feature.{Op, State}
import io.findify.featury.model.Schema.FeatureConfig

trait Feature[S <: State, T <: FeatureValue, O <: Op, C <: FeatureConfig] {
  def emptyState(conf: C): S
  def value(conf: C, state: S): T
  def update(conf: C, state: S, op: O): S
}

object Feature {
  trait Op {
    def ts: Timestamp
  }
  trait State {}
}
