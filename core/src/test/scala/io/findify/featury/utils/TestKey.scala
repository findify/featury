package io.findify.featury.utils

import io.findify.featury.model.{FeatureConfig, Key}
import io.findify.featury.model.Key._

import scala.util.Random

object TestKey {
  def apply(c: FeatureConfig, id: String) = {
    Key(
      tag = Tag(c.scope, id),
      name = c.name,
      tenant = Tenant("1")
    )
  }
  def apply(
      scope: String = "product",
      fname: String = "f" + Random.nextInt(1000),
      tenant: Int = 1,
      id: String = "p1"
  ) = Key(
    tag = Tag(Scope(scope), id),
    name = FeatureName(fname),
    tenant = Tenant(tenant.toString)
  )
}
