package io.findify.featury.model.json

import io.circe.{Codec, Decoder, Encoder, Json, JsonObject}
import io.findify.featury.model.Key
import io.circe.generic.semiauto._
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}

trait KeyJson {

  implicit val tagCodec: Codec[Tag]          = deriveCodec[Tag]
  implicit val scopeCodec: Codec[Scope]      = stringCodec(_.name, Scope.apply)
  implicit val nameCodec: Codec[FeatureName] = stringCodec(_.value, FeatureName.apply)
  implicit val tenantCodec: Codec[Tenant]    = stringCodec(_.value, Tenant.apply)

  implicit val keyEncoder: Encoder[Key] = Encoder.instance(key =>
    Json.fromJsonObject(
      JsonObject.fromMap(
        Map(
          "scope"  -> scopeCodec(key.tag.scope),
          "id"     -> Json.fromString(key.tag.value),
          "name"   -> nameCodec(key.name),
          "tenant" -> tenantCodec(key.tenant)
        )
      )
    )
  )

  implicit val keyDecoder: Decoder[Key] = Decoder.instance(c =>
    for {
      scope  <- c.downField("scope").as[Scope]
      id     <- c.downField("id").as[String]
      name   <- c.downField("name").as[FeatureName]
      tenant <- c.downField("tenant").as[Tenant]
    } yield {
      Key(Tag(scope, id), name, tenant)
    }
  )

  implicit val keyCodec: Codec[Key] = Codec.from(keyDecoder, keyEncoder)

  def stringCodec[T](toString: T => String, fromString: String => T) =
    Codec.from(Decoder.decodeString.map(fromString), Encoder.encodeString.contramap(toString))
}
