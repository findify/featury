package io.findify.featury.model

import io.findify.featury.model.Key.{Id, Tenant}

trait KeyCompanionOps {
  def apply(conf: FeatureConfig, tenant: Tenant, id: Id): Key = new Key(
    ns = conf.ns,
    scope = conf.scope,
    name = conf.name,
    tenant = tenant,
    id = id
  )
}
