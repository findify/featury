package io.findify.featury.model.json

import io.circe.generic.extras.Configuration
import io.circe.{Codec, Encoder}
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.findify.featury.model.PeriodicCounterValue.PeriodicValue
import io.findify.featury.model._

object FeatureValueJson {

  import ScalarJson._
  implicit val scalarCodec: Codec[ScalarValue]                        = deriveCodec
  implicit val counterCodec: Codec[CounterValue]                      = deriveCodec
  implicit val numStatsCodec: Codec[NumStatsValue]                    = deriveCodec
  implicit val periodicValueCodec: Codec[PeriodicValue]               = deriveCodec
  implicit val periodicCounterValueCodec: Codec[PeriodicCounterValue] = deriveCodec
  implicit val timeValueCodec: Codec[TimeValue]                       = deriveCodec
  implicit val boundedListCodec: Codec[BoundedListValue]              = deriveCodec

  implicit val config = Configuration.default
    .withDiscriminator("type")
    .copy(transformConstructorNames = {
      case "FrequencyValue"       => "freq"
      case "ScalarValue"          => "scalar"
      case "CounterValue"         => "counter"
      case "NumStatsValue"        => "stats"
      case "PeriodicCounterValue" => "periodic_counter"
      case "BoundedListValue"     => "list"
    })

  implicit val featureValueEncoder: Codec[FeatureValue] = deriveConfiguredCodec
}
