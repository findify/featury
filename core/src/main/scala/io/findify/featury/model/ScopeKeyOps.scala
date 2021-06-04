package io.findify.featury.model

import io.findify.featury.model.Key.{Scope, Id, Namespace, Tenant}

trait ScopeKeyOps {
  def apply(key: Key): ScopeKey = ScopeKey(key.ns, key.group, key.tenant, key.id)
  def make(ns: String, group: String, tenant: String, id: String): ScopeKey =
    ScopeKey(Namespace(ns), Scope(group), Tenant(tenant), Id(id))
}
