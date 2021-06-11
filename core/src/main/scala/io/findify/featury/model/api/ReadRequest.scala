package io.findify.featury.model.api

import io.circe.Codec
import io.circe.generic.semiauto._
import io.findify.featury.model.Key.{FeatureName, Id, Namespace, Scope, Tenant}

case class ReadRequest(ns: Namespace, scope: Scope, tenant: Tenant, features: List[FeatureName], ids: List[Id])

object ReadRequest {
  implicit val readRequestCodec: Codec[ReadRequest] = deriveCodec
}
