package io.findify.featury.util

import io.findify.featury.model.Key
import io.findify.featury.persistence.ValueStore.KeyBatch

object TestKeyBatch {
  def apply(key: Key) = KeyBatch(
    ns = key.ns,
    group = key.group,
    featureNames = List(key.featureName),
    tenant = key.tenant,
    ids = List(key.id)
  )
}
