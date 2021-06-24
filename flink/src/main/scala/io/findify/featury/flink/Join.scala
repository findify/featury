package io.findify.featury.flink

import io.findify.featury.model.Key.Scope
import io.findify.featury.model.{FeatureValue, ScopeKey}

trait Join[T] extends Serializable {
  def appendValues(self: T, values: List[FeatureValue]): T
  def scopedKey(value: T, scope: Scope): Option[ScopeKey]
}
