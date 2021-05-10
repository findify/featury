package io.findify.featury.util

import io.findify.featury.model.Key
import io.findify.featury.model.Key._

import scala.util.Random

object TestKey {
  def apply(
      ns: String = "dev",
      group: String = "product",
      fname: String = "f" + Random.nextInt(1000),
      tenant: Int = 1,
      id: String = "p1"
  ) = Key(
    ns = Namespace(ns),
    group = GroupName(group),
    featureName = FeatureName(fname),
    tenant = Tenant(tenant),
    id = KeyId(id)
  )
}
