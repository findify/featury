package io.findify.featury.model

import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Key.FeatureKey
import io.findify.featury.model.ScalarType.{NumType, TextType}

case class ConfigMap(
    counters: Map[FeatureKey, CounterConfig],
    strings: Map[FeatureKey, ScalarConfig],
    doubles: Map[FeatureKey, ScalarConfig]
) {}

object ConfigMap {
  def apply(schema: Schema) = {
    val configs = for {
      ns      <- schema.namespaces
      group   <- ns.groups
      feature <- group.features
    } yield {
      FeatureKey(ns.name, group.name, feature.name) -> feature
    }
    new ConfigMap(
      counters = configs.collect { case (key, c: CounterConfig) => key -> c }.toMap,
      strings = configs.collect { case (key, c: ScalarConfig) if c.contentType == TextType => key -> c }.toMap,
      doubles = configs.collect { case (key, c: ScalarConfig) if c.contentType == NumType => key -> c }.toMap
    )
  }
}
