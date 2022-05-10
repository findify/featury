package io.findify.featury.model.json

import io.circe.{Codec, Decoder, DecodingFailure, Encoder, Json}
import io.findify.featury.model.{SBoolean, SDouble, SDoubleList, SString, SStringList, Scalar}
import cats.implicits._

object ScalarJson {

  implicit val stringCodec: Codec[SString] =
    Codec.from(Decoder.decodeString.map(SString.apply), Encoder.encodeString.contramap[SString](_.value))

  implicit val doubleCodec: Codec[SDouble] =
    Codec.from(Decoder.decodeDouble.map(SDouble.apply), Encoder.encodeDouble.contramap[SDouble](_.value))

  implicit val stringListCodec: Codec[SStringList] =
    Codec.from(
      decodeA = Decoder.decodeList[String].map(SStringList.apply),
      encodeA = Encoder.encodeList[String].contramap(_.value.toList)
    )

  implicit val doubleListCodec: Codec[SDoubleList] =
    Codec.from(
      decodeA = Decoder.decodeList[Double].map(SDoubleList.apply),
      encodeA = Encoder.encodeList[Double].contramap(_.value.toList)
    )

  implicit val booleanCodec: Codec[SBoolean] =
    Codec.from(Decoder.decodeBoolean.map(SBoolean.apply), Encoder.encodeBoolean.contramap[SBoolean](_.value))

  implicit val scalarEncoder: Encoder[Scalar] = Encoder.instance {
    case s: SString      => stringCodec(s)
    case d: SDouble      => doubleCodec(d)
    case b: SBoolean     => booleanCodec(b)
    case sl: SStringList => stringListCodec(sl)
    case dl: SDoubleList => doubleListCodec(dl)
    case Scalar.Empty    => Json.Null // this should not happen
  }

  implicit val scalarDecoder: Decoder[Scalar] = Decoder.instance(c =>
    c.value.fold(
      jsonNull = Left(DecodingFailure("null is not supported", c.history)),
      jsonBoolean = b => Right(SBoolean(b)),
      jsonNumber = n => Right(SDouble(n.toDouble)),
      jsonString = s => Right(SString(s)),
      jsonObject = o => Left(DecodingFailure(s"cannot decode object $o as scalar", c.history)),
      jsonArray = array =>
        array.headOption match {
          case Some(x) if x.isString => array.map(_.as[String]).sequence.map(vec => SStringList(vec.toList))
          case Some(x) if x.isNumber => array.map(_.as[Double]).sequence.map(vec => SDoubleList(vec.toList))
          case Some(x)               => Left(DecodingFailure(s"cannot decode list of $x", c.history))
          case None                  => Left(DecodingFailure("cannot decode empty list", c.history))
        }
    )
  )

  implicit val scalarCodec: Codec[Scalar] = Codec.from(scalarDecoder, scalarEncoder)

}
