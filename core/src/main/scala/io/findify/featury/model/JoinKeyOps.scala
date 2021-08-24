package io.findify.featury.model

import io.findify.featury.model.Key.{Namespace, Scope, Tenant}

trait JoinKeyOps {
  def apply(key: Key): JoinKey = JoinKey(key.ns, key.tenant)
  def make(ns: String, tenant: String) =
    Option(JoinKey(Namespace(ns), Tenant(tenant)))
}
