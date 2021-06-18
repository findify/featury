package io.findify.featury.model

import io.findify.featury.model.Key.{FeatureName, Scope, Namespace}

case class FeatureKey(ns: Namespace, scope: Scope, feature: FeatureName) {
  def fqdn = s"${ns.value}/${scope.value}/${feature.value}"
}

object FeatureKey {
  def apply(key: Key): FeatureKey = FeatureKey(key.ns, key.scope, key.name)
}
