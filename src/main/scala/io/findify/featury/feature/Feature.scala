package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Feature.State
import io.findify.featury.model.Schema.FeatureConfig
import io.findify.featury.model.{FeatureValue, Key}

trait Feature[S <: State, T <: FeatureValue, C <: FeatureConfig] {
  def config: C
  def empty(): S
  def readState(key: Key): IO[S]
  def computeValue(state: S): Option[T]
}

object Feature {
  trait State {}
}
