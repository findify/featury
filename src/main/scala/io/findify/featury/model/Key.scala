package io.findify.featury.model

import io.findify.featury.model.Key._

case class Key(ns: Namespace, group: GroupName, featureName: FeatureName, tenant: Tenant, id: KeyId)

object Key {
  case class Namespace(value: String)   extends AnyVal
  case class GroupName(value: String)   extends AnyVal
  case class FeatureName(value: String) extends AnyVal
  case class Tenant(value: Int)         extends AnyVal
  case class KeyId(value: String)       extends AnyVal
}
