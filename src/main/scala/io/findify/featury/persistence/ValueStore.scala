package io.findify.featury.persistence

import io.findify.featury.model.{FeatureValue, Key}
import cats.effect.IO
import io.findify.featury.model.Key.{FeatureName, GroupName, KeyId, Namespace, Tenant}
import io.findify.featury.model.ReadResponse.ItemFeatures
import io.findify.featury.persistence.ValueStore.{BatchResult, KeyBatch}

trait ValueStore {
  def write(key: Key, value: FeatureValue): IO[Unit]
  def readBatch(keys: KeyBatch): IO[BatchResult]
}

object ValueStore {
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

  case class KeyFeatures(id: KeyId, features: Map[FeatureName, FeatureValue])
  case class BatchResult(ns: Namespace, group: GroupName, values: List[KeyFeatures])
}
