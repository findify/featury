package io.findify.featury.model

import io.findify.featury.model.Key.{GroupName, Id, Namespace, Tenant}

trait ScopeKeyOps {
  def apply(key: Key): ScopeKey = ScopeKey(key.ns, key.group, key.tenant, key.id)
  def make(ns: String, group: String, tenant: Int, id: String): ScopeKey =
    ScopeKey(Namespace(ns), GroupName(group), Tenant(tenant), Id(id))
}
