package io.findify.featury.utils

import io.findify.featury.model.{FeatureConfig, Key}
import io.findify.featury.model.Key._

import scala.util.Random

object TestKey {
  def apply(c: FeatureConfig, id: String) = {
    Key(
      ns = c.ns,
      group = c.group,
      name = c.name,
      tenant = Tenant("1"),
      id = Id(id)
    )
  }
  def apply(
      ns: String = "dev",
      group: String = "product",
      fname: String = "f" + Random.nextInt(1000),
      tenant: Int = 1,
      id: String = "p1"
  ) = Key(
    ns = Namespace(ns),
    group = Scope(group),
    name = FeatureName(fname),
    tenant = Tenant(tenant.toString),
    id = Id(id)
  )
}
