package io.findify.featury.flink.util

import io.findify.featury.model.Key
import io.findify.featury.model.Key.{GroupName, Id, Namespace, Tenant}

case class ScopeKey(ns: Namespace, group: GroupName, tenant: Tenant, id: Id)

object ScopeKey {
  def apply(key: Key): ScopeKey = ScopeKey(key.ns, key.group, key.tenant, key.id)
  def make(ns: String, group: String, tenant: Int, id: String): ScopeKey =
    ScopeKey(Namespace(ns), GroupName(group), Tenant(tenant), Id(id))
}
