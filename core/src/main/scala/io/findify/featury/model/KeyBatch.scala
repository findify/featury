package io.findify.featury.model

import io.findify.featury.model.Key.{FeatureName, GroupName, KeyId, Namespace, Tenant}

case class KeyBatch(
    ns: Namespace,
    group: GroupName,
    featureNames: List[FeatureName],
    tenant: Tenant,
    ids: List[KeyId]
) {
  def asKey(name: FeatureName, id: KeyId) = Key(ns, group, name, tenant, id)
  def asKeys: List[Key] = for {
    id      <- ids
    feature <- featureNames
  } yield {
    Key(ns, group, feature, tenant, id)
  }
}
