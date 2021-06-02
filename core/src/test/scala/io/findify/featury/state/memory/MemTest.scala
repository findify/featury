package io.findify.featury.state.memory

import io.findify.featury.features.FeatureSuite
import io.findify.featury.model.{Feature, FeatureValue, Write}

trait MemTest[W <: Write, F <: Feature[W, _ <: FeatureValue, _, _]] { this: FeatureSuite[W] =>
  def feature: F
  def write(values: List[W]): Option[FeatureValue] = {
    values.foldLeft(Option.empty[FeatureValue])((_, inc) => {
      feature.put(inc)
      feature.computeValue(inc.key, inc.ts)
    })
  }
}
