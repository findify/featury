package io.findify.featury.model

import io.findify.featury.model.Key.{Scope, Id, Namespace, Tenant}

trait ScopeKeyOps {
  def apply(key: Key): ScopeKey = ScopeKey(key.ns, key.scope, key.tenant, key.id)
  def make(ns: String, scope: String, tenant: String, id: String) =
    Option(ScopeKey(Namespace(ns), Scope(scope), Tenant(tenant), Id(id)))
}
