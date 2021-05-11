package io.findify.featury.persistence.codec

import io.findify.featury.model.FeatureValue.{Num, Scalar, Text}
import io.findify.featury.persistence.codec.Codec.DecodingError

import scala.util.{Failure, Success, Try}

trait Codec[T <: Scalar] {
  def encode(value: T): String
  def decode(in: String): Either[DecodingError, T]
}

object Codec {
  case class DecodingError(msg: String) extends Throwable(msg)

  implicit val textCodec: Codec[Text] = new Codec[Text] {
    override def encode(value: Text): String                     = value.value
    override def decode(in: String): Either[DecodingError, Text] = Right(Text(in))
  }

  implicit val numCodec: Codec[Num] = new Codec[Num] {
    override def encode(value: Num): String = value.value.toString
    override def decode(in: String): Either[DecodingError, Num] = Try(java.lang.Double.parseDouble(in)) match {
      case Failure(exception) => Left(DecodingError(s"cannot decode double $in: $exception"))
      case Success(value)     => Right(Num(value))
    }
  }
}
