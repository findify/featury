package io.findify.featury.model.json

import io.circe.{Codec, Decoder, Encoder, Json}
import io.findify.featury.model.{SBoolean, SDouble, SString, Scalar}

object ScalarJson {

  implicit val stringCodec: Codec[SString] =
    Codec.from(Decoder.decodeString.map(SString.apply), Encoder.encodeString.contramap[SString](_.value))

  implicit val doubleCodec: Codec[SDouble] =
    Codec.from(Decoder.decodeDouble.map(SDouble.apply), Encoder.encodeDouble.contramap[SDouble](_.value))

  implicit val booleanCodec: Codec[SBoolean] =
    Codec.from(Decoder.decodeBoolean.map(SBoolean.apply), Encoder.encodeBoolean.contramap[SBoolean](_.value))

  implicit val scalarEncoder: Encoder[Scalar] = Encoder.instance {
    case s: SString   => stringCodec(s)
    case d: SDouble   => doubleCodec(d)
    case b: SBoolean  => booleanCodec(b)
    case Scalar.Empty => Json.Null // this should not happen
  }

  implicit val scalarDecoder: Decoder[Scalar] = Decoder.instance(c =>
    stringCodec.tryDecode(c) match {
      case Left(_) =>
        doubleCodec.tryDecode(c) match {
          case err @ Left(_) => err
          case value         => value
        }
      case value => value
    }
  )

  implicit val scalarCodec: Codec[Scalar] = Codec.from(scalarDecoder, scalarEncoder)

}
