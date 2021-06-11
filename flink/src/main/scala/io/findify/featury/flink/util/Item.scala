package io.findify.featury.flink.util

import io.findify.featury.model.FeatureValue

trait Item[T] {
  def values: List[FeatureValue]
  def appendValues(list: List[FeatureValue]): T
}
