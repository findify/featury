package io.findify.featury.model

import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.{GroupSchema, NamespaceSchema}

case class Schema(namespaces: List[NamespaceSchema])

object Schema {
  case class NamespaceSchema(name: Namespace, groups: List[GroupSchema])
  case class GroupSchema(name: GroupName, features: List[FeatureConfig])

  trait FeatureConfig {
    def ns: Namespace
    def group: GroupName
    def name: FeatureName
  }
}
