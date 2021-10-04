package io.findify.featury.model

import io.findify.featury.model.Key.{Tag, Tenant}

trait KeyCompanionOps {
  def apply(conf: FeatureConfig, tenant: Tenant, id: String): Key = new Key(
    tag = Tag(conf.scope, id),
    name = conf.name,
    tenant = tenant
  )
}
