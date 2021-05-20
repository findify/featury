package io.findify.featury.model

import io.findify.featury.model.Key.{GroupName, Namespace}
import io.findify.featury.model.Schema.NamespaceSchema

case class Schema(namespaces: List[NamespaceSchema])

object Schema {
  case class NamespaceSchema(name: Namespace, groups: List[GroupSchema])
  case class GroupSchema(name: GroupName, features: List[FeatureConfig])

}
