package io.findify.featury.model.json

import io.circe.{Codec, Decoder, Encoder}
import io.findify.featury.model.Key
import io.circe.generic.semiauto._
import io.findify.featury.model.Key.{FeatureName, Id, Namespace, Scope, Tenant}

trait KeyJson {

  implicit val nsCodec: Codec[Namespace]     = stringCodec(_.value, Namespace.apply)
  implicit val scopeCodec: Codec[Scope]      = stringCodec(_.value, Scope.apply)
  implicit val nameCodec: Codec[FeatureName] = stringCodec(_.value, FeatureName.apply)
  implicit val tenantCodec: Codec[Tenant]    = stringCodec(_.value, Tenant.apply)
  implicit val idCodec: Codec[Id]            = stringCodec(_.value, Id.apply)

  implicit val keyCodec: Codec[Key] = deriveCodec[Key]

  def stringCodec[T](toString: T => String, fromString: String => T) =
    Codec.from(Decoder.decodeString.map(fromString), Encoder.encodeString.contramap(toString))
}
