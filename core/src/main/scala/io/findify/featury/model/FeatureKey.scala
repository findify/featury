package io.findify.featury.model

import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}

case class FeatureKey(ns: Namespace, group: GroupName, feature: FeatureName) {
  def fqdn = s"${ns.value}/${group.value}/${feature.value}"
}

object FeatureKey {
  def apply(key: Key): FeatureKey = FeatureKey(key.ns, key.group, key.name)
}
