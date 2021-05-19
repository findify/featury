package io.findify.featury.persistence.redis

import io.findify.featury.model.FeatureValue._
import io.findify.featury.model.{FeatureValue, Timestamp}
import io.findify.featury.persistence.redis.Codec.DecodingError

import scala.util.{Failure, Success, Try}

trait Codec[T] {
  def encode(value: T): String
  def decode(in: String): Either[DecodingError, T]
}

object Codec {
  case class DecodingError(msg: String) extends Throwable(msg)
  val RS = "\u001e" // record separator char
  val GS = "\u001d" // group separator char

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
  /*
  case class ScalarValue[T <: Scalar](value: T)                                      extends FeatureValue
  case class NumStatsValue(min: Double, max: Double, quantiles: Map[Double, Double]) extends FeatureValue
  case class PeriodicNumValue(values: Map[Long, Double])                             extends FeatureValue
  case class StringFrequencyValue(values: Map[String, Double])                       extends FeatureValue
  case class BoundedListValue[T <: Scalar](values: List[ListItem[T]])                extends FeatureValue

   */
  implicit def listItemCodec[T <: Scalar](implicit c: Codec[T]): Codec[ListItem[T]] = new Codec[ListItem[T]] {
    override def encode(value: ListItem[T]): String = s"${value.ts.ts}$RS${c.encode(value.value)}"

    override def decode(in: String): Either[DecodingError, ListItem[T]] = {
      val firstSeparator = in.indexOf(RS)
      if (firstSeparator <= 0) {
        Left(DecodingError(s"cannot decode list item $in"))
      } else {
        val ts    = in.substring(0, firstSeparator)
        val value = in.substring(firstSeparator + RS.length, in.length)
        Try(java.lang.Long.parseLong(ts)) match {
          case Failure(exception) => Left(DecodingError(s"wrong timestamp: ${ts} due to $exception"))
          case Success(ts) =>
            c.decode(value) match {
              case Left(err)    => Left(DecodingError(s"cannot decode value $value: $err"))
              case Right(value) => Right(ListItem(value, Timestamp(ts)))
            }
        }
      }
    }
  }

  sealed trait ValueCodec[T <: FeatureValue] extends Codec[T]

  implicit val scalarTextCodec: ValueCodec[ScalarValue[Text]] = new ValueCodec[ScalarValue[Text]] {
    override def encode(value: ScalarValue[Text]): String = textCodec.encode(value.value)
    override def decode(in: String): Either[DecodingError, ScalarValue[Text]] =
      textCodec.decode(in).map(ScalarValue.apply)
  }
  implicit val scalarNumCodec: ValueCodec[ScalarValue[Num]] = new ValueCodec[ScalarValue[Num]] {
    override def encode(value: ScalarValue[Num]): String = numCodec.encode(value.value)
    override def decode(in: String): Either[DecodingError, ScalarValue[Num]] =
      numCodec.decode(in).map(ScalarValue.apply)
  }

  def listValueCodec[T <: Scalar, F <: BoundedListValue[T]](
      make: List[ListItem[T]] => F
  )(implicit c: Codec[ListItem[T]]): ValueCodec[F] =
    new ValueCodec[F] {
      override def encode(value: F): String =
        value.values.map(item => c.encode(item)).mkString(GS)

      override def decode(in: String): Either[DecodingError, F] =
        in.split(GS)
          .map(item => c.decode(item))
          .toList
          .foldLeft[Either[DecodingError, List[ListItem[T]]]](Right(List.empty)) {
            case (Left(err), _)            => Left(err)
            case (Right(acc), Right(next)) => Right(next :: acc)
            case (_, Left(err))            => Left(err)
          }
          .map(make)
    }

  implicit val textListValueCodec = listValueCodec[Text, TextBoundedListValue](TextBoundedListValue.apply)
  implicit val numListValueCodec  = listValueCodec[Num, NumBoundedListValue](NumBoundedListValue.apply)

  implicit val valueCodec: Codec[FeatureValue] = new Codec[FeatureValue] {
    override def encode(value: FeatureValue): String = value match {
      case value: TextScalarValue      => "t:" + scalarTextCodec.encode(value)
      case value: NumScalarValue       => "n:" + scalarNumCodec.encode(value)
      case value: TextBoundedListValue => "tl:" + textListValueCodec.encode(value)
      case value: NumBoundedListValue  => "nl:" + numListValueCodec.encode(value)
      case _                           => "" // todo
      //      case NumStatsValue(min, max, quantiles) =>
      //      case PeriodicNumValue(values) =>
      //      case StringFrequencyValue(values) =>
    }

    override def decode(in: String): Either[DecodingError, FeatureValue] = {
      val sepPosition = in.indexOf(":")
      if (sepPosition <= 0) {
        Left(DecodingError(s"cannot decode $in"))
      } else {
        val tpe   = in.substring(0, sepPosition)
        val value = in.substring(sepPosition + 1, in.length)
        tpe match {
          case "t"  => scalarTextCodec.decode(value)
          case "n"  => scalarNumCodec.decode(value)
          case "tl" => textListValueCodec.decode(value)
          case "nl" => numListValueCodec.decode(value)
          case _    => Left(DecodingError(s"cannot decode value of type $tpe"))
        }
      }
    }
  }
}
