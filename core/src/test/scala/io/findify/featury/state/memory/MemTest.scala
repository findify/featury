package io.findify.featury.state.memory

import io.findify.featury.features.FeatureSuite
import io.findify.featury.model.{Feature, FeatureValue, Write}

trait MemTest[W <: Write, T <: FeatureValue, F <: Feature[W, T, _, _]] { this: FeatureSuite[W, T] =>
  def feature: F
  def write(values: List[W]): Option[T] = {
    values.foldLeft(Option.empty[T])((_, inc) => {
      feature.put(inc)
      feature.computeValue(inc.key, inc.ts)
    })
  }
}
