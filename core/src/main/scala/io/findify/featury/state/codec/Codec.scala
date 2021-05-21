package io.findify.featury.state.codec

import io.findify.featury.model.FeatureValue.ScalarValue.{DoubleScalarValue, LongScalarValue, StringScalarValue}
import io.findify.featury.model.FeatureValue.{BoundedListValue, ListItem, ScalarValue}
import io.findify.featury.model.{BackendError, FeatureValue, SDouble, SLong, SString, Scalar, Timestamp}
import io.findify.featury.state.codec.Codec.DecodingError

import scala.util.{Failure, Success, Try}

trait Codec[T] {
  def encode(value: T): String
  def decode(string: String): Either[DecodingError, T]
}

object Codec {
  case class DecodingError(msg: String) extends Throwable(msg)
  val RS = "\u001e" // record separator char
  val GS = "\u001d" // group separator char
  implicit val stringCodec: Codec[SString] = new Codec[SString] {
    override def encode(value: SString): String                     = value.value
    override def decode(in: String): Either[DecodingError, SString] = Right(SString(in))
  }

  implicit val doubleCodec: Codec[SDouble] = new Codec[SDouble] {
    override def encode(value: SDouble): String = value.value.toString
    override def decode(in: String): Either[DecodingError, SDouble] = Try(java.lang.Double.parseDouble(in)) match {
      case Failure(exception) => Left(DecodingError(s"cannot decode double $in: $exception"))
      case Success(value)     => Right(SDouble(value))
    }
  }

  implicit val longCodec: Codec[SLong] = new Codec[SLong] {
    override def encode(value: SLong): String = value.value.toString
    override def decode(in: String): Either[DecodingError, SLong] = Try(java.lang.Long.parseLong(in)) match {
      case Failure(exception) => Left(DecodingError(s"cannot decode double $in: $exception"))
      case Success(value)     => Right(SLong(value))
    }
  }

  sealed trait ValueCodec[T <: FeatureValue] extends Codec[T]

  implicit val scalarStringCodec: ValueCodec[ScalarValue[SString]] = new ValueCodec[ScalarValue[SString]] {
    override def encode(value: ScalarValue[SString]): String = stringCodec.encode(value.value)
    override def decode(in: String): Either[DecodingError, ScalarValue[SString]] =
      stringCodec.decode(in).map(StringScalarValue.apply)
  }
  implicit val scalarDoubleCodec: ValueCodec[ScalarValue[SDouble]] = new ValueCodec[ScalarValue[SDouble]] {
    override def encode(value: ScalarValue[SDouble]): String = doubleCodec.encode(value.value)
    override def decode(in: String): Either[DecodingError, ScalarValue[SDouble]] =
      doubleCodec.decode(in).map(DoubleScalarValue.apply)
  }
  implicit val scalarLongCodec: ValueCodec[ScalarValue[SLong]] = new ValueCodec[ScalarValue[SLong]] {
    override def encode(value: ScalarValue[SLong]): String = longCodec.encode(value.value)
    override def decode(in: String): Either[DecodingError, ScalarValue[SLong]] =
      longCodec.decode(in).map(LongScalarValue.apply)
  }
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
  def listValueCodec[T <: Scalar, F <: BoundedListValue[T]](
      make: List[ListItem[T]] => F
  )(implicit c: Codec[ListItem[T]]): ValueCodec[F] =
    new ValueCodec[F] {
      override def encode(value: F): String =
        value.value.map(item => c.encode(item)).mkString(GS)

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

  implicit val textListValueCodec =
    listValueCodec[SString, BoundedListValue[SString]](list => new BoundedListValue[SString](list))
  implicit val numListValueCodec =
    listValueCodec[SDouble, BoundedListValue[SDouble]](list => new BoundedListValue[SDouble](list))

  implicit val valueCodec: Codec[FeatureValue] = new Codec[FeatureValue] {
    override def encode(value: FeatureValue): String = value match {
      case value: StringScalarValue => "s:" + scalarStringCodec.encode(value)
      case value: DoubleScalarValue => "d:" + scalarDoubleCodec.encode(value)
      case value: LongScalarValue   => "l:" + scalarLongCodec.encode(value)
//      case value: TextBoundedListValue => "tl:" + textListValueCodec.encode(value)
//      case value: NumBoundedListValue  => "nl:" + numListValueCodec.encode(value)
      case _ => "" // todo
      //      case NumStatsValue(min, max, quantiles) =>
      //      case PeriodicNumValue(values) =>
      //      case StringFrequencyValue(values) =>
    }

    override def decode(string: String): Either[DecodingError, FeatureValue] = {
      val sepIndex = string.indexOf(":")
      if (sepIndex <= 0) {
        Left(DecodingError(s"incorrect format: $string"))
      } else {
        val valueType   = string.substring(0, sepIndex)
        val valueString = string.substring(sepIndex + 1, string.length)
        valueType match {
          case "s" => scalarStringCodec.decode(valueString)
          case "d" => scalarDoubleCodec.decode(valueString)
          case "l" => scalarLongCodec.decode(valueString)
          case _   => Left(DecodingError("not supported yet"))
        }
      }
    }
  }
}
