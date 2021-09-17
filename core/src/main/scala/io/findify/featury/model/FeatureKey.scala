package io.findify.featury.model

import io.findify.featury.model.Key.{FeatureName, Scope}

case class FeatureKey(scope: Scope, feature: FeatureName) {
  def fqdn = s"${scope.name}/${feature.value}"
}

object FeatureKey {
  def apply(key: Key): FeatureKey = FeatureKey(key.tag.scope, key.name)
}
