package io.findify.featury.model

import io.findify.featury.model.Key.{FeatureName, Scope, Id, Namespace, Tenant}

case class KeyBatch(
    ns: Namespace,
    group: Scope,
    featureNames: List[FeatureName],
    tenant: Tenant,
    ids: List[Id]
) {
  def asKey(name: FeatureName, id: Id) = Key(ns, group, name, tenant, id)
  def asKeys: List[Key] = for {
    id      <- ids
    feature <- featureNames
  } yield {
    Key(ns, group, feature, tenant, id)
  }
}
