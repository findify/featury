package io.findify.featury.model.json

import io.circe.{Codec, Decoder, DecodingFailure, Encoder}
import io.findify.featury.model._
import io.circe.generic.semiauto._
import io.findify.featury.model.PeriodicCounterValue.PeriodicValue

object FeatureValueJson {
  import ScalarJson._
  import io.findify.featury.model.json.KeyJson._
  import io.findify.featury.model.json.TimestampJson._

  implicit val scalarCodec: Codec[ScalarValue]                        = deriveCodec
  implicit val freqCodec: Codec[FrequencyValue]                       = deriveCodec
  implicit val counterCodec: Codec[CounterValue]                      = deriveCodec
  implicit val numStatsCodec: Codec[NumStatsValue]                    = deriveCodec
  implicit val periodicValueCodec: Codec[PeriodicValue]               = deriveCodec
  implicit val periodicCounterValueCodec: Codec[PeriodicCounterValue] = deriveCodec
  implicit val timeValueCodec: Codec[TimeValue]                       = deriveCodec
  implicit val boundedListCodec: Codec[BoundedListValue]              = deriveCodec
  implicit val mapCodec: Codec[MapValue]                              = deriveCodec

  implicit val featureValueEncoder: Encoder[FeatureValue] = Encoder.instance {
    case v: ScalarValue          => scalarCodec.apply(v)
    case v: CounterValue         => counterCodec.apply(v)
    case v: NumStatsValue        => numStatsCodec.apply(v)
    case v: MapValue             => mapCodec.apply(v)
    case v: PeriodicCounterValue => periodicCounterValueCodec.apply(v)
    case v: FrequencyValue       => freqCodec.apply(v)
    case v: BoundedListValue     => boundedListCodec.apply(v)
  }

  implicit val featureValueDecoder: Decoder[FeatureValue] = Decoder.instance(c =>
    for {
      tpe <- c.downField("type").as[String]
      decoded <- tpe match {
        case "freq"             => freqCodec.tryDecode(c)
        case "scalar"           => scalarCodec.tryDecode(c)
        case "counter"          => counterCodec.tryDecode(c)
        case "stats"            => numStatsCodec.tryDecode(c)
        case "periodic_counter" => periodicCounterValueCodec.tryDecode(c)
        case "list"             => boundedListCodec.tryDecode(c)
        case "map"              => mapCodec.tryDecode(c)
        case other              => Left(DecodingFailure(s"value type $other is not supported", c.history))
      }
    } yield {
      decoded
    }
  )

  implicit val featureValueCodec: Codec[FeatureValue] = Codec.from(featureValueDecoder, featureValueEncoder)
}
