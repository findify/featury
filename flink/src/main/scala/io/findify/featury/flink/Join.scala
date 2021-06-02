package io.findify.featury.flink

import io.findify.featury.flink.Join.Scope
import io.findify.featury.model.{FeatureValue, ScopeKey}

trait Join[T] extends Serializable {
  def appendValues(self: T, values: List[FeatureValue]): T
  def scopedKey(value: T, scope: Scope): ScopeKey

}

object Join {
  trait Scope extends Serializable
}
