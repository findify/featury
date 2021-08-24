package io.findify.featury.model.api

import io.circe.Codec
import io.circe.generic.semiauto._
import io.findify.featury.model.Key.{FeatureName, Namespace, Tag, Tenant}

case class ReadRequest(ns: Namespace, tags: List[Tag], tenant: Tenant, features: List[FeatureName])

object ReadRequest {
  implicit val readRequestCodec: Codec[ReadRequest] = deriveCodec
}
