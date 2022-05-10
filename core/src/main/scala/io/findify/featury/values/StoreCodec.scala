package io.findify.featury.values

import io.findify.featury.model.{FeatureValue, FeatureValueMessage}
import io.findify.featury.values.StoreCodec.DecodingError
import io.circe.syntax._
import io.circe.parser._

import java.nio.charset.StandardCharsets
import scala.util.{Failure, Success, Try}

sealed trait StoreCodec {
  def encode(value: FeatureValue): Array[Byte]
  def decode(bytes: Array[Byte]): Either[DecodingError, FeatureValue]
}

object StoreCodec {
  case class DecodingError(msg: String) extends Exception(msg)

  case object ProtobufCodec extends StoreCodec {
    override def encode(value: FeatureValue): Array[Byte] = value.asMessage.toByteArray

    override def decode(bytes: Array[Byte]): Either[DecodingError, FeatureValue] =
      Try(FeatureValueMessage.parseFrom(bytes)) match {
        case Failure(exception) => Left(DecodingError(exception.getMessage))
        case Success(value) =>
          value.toFeatureValue match {
            case Some(value) => Right(value)
            case None        => Left(DecodingError("empty feature value protobuf"))
          }
      }
  }
}
