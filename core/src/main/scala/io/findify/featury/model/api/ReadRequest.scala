package io.findify.featury.model.api

import io.circe.Codec
import io.circe.generic.semiauto._
import io.findify.featury.model.Key
import io.findify.featury.model.Key.{FeatureName, Tag, Tenant}

case class ReadRequest(keys: List[Key])

object ReadRequest {
  implicit val readRequestCodec: Codec[ReadRequest] = deriveCodec
}
