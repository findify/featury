package io.findify.featury.model.api

import io.circe.Codec
import io.circe.generic.semiauto._
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.Key.{Namespace, Scope, Tenant}

case class ReadResponse(features: List[FeatureValue]) {}

object ReadResponse {
  import io.findify.featury.model.json.FeatureValueJson._
  implicit val readResponseCodec: Codec[ReadResponse] = deriveCodec
}
