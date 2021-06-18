package io.findify.featury.model

import io.circe.{Decoder, DecodingFailure}
import io.findify.featury.model.Key._

import scala.concurrent.duration._
import io.circe.generic.semiauto._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Try}

sealed trait FeatureConfig {
  def ns: Namespace
  def scope: Scope
  def name: FeatureName
  def ttl: FiniteDuration
  def refresh: FiniteDuration
  def monitorLag: Boolean
  def fqdn = s"${ns.value}.${scope.value}.${name.value}"
}

object FeatureConfig {
  case class MonitorValuesConfig(min: Double, max: Double, buckets: Int) {
    lazy val bucketList = {
      val buffer = ArrayBuffer[Double]()
      val step   = (max - min) / buckets
      var i      = min
      while (i <= max) {
        buffer.append(i)
        i += step
      }
      buffer.toList
    }
  }

  case class CounterConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorValues: Option[MonitorValuesConfig] = None,
      monitorLag: Boolean = false
  ) extends FeatureConfig

  case class ScalarConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorValues: Option[MonitorValuesConfig] = None,
      monitorLag: Boolean = false
  ) extends FeatureConfig

  case class MapConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorLag: Boolean = false,
      monitorValues: Option[MonitorValuesConfig] = None,
      monitorSize: Boolean = false
  ) extends FeatureConfig

  case class BoundedListConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      count: Int = Int.MaxValue,
      duration: FiniteDuration = Long.MaxValue.nanos,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorLag: Boolean = false,
      monitorSize: Boolean = false
  ) extends FeatureConfig

  case class FreqEstimatorConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      poolSize: Int,
      sampleRate: Int,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorLag: Boolean = false,
      monitorSize: Boolean = false
  ) extends FeatureConfig

  case class PeriodRange(startOffset: Int, endOffset: Int)
  case class PeriodicCounterConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      period: FiniteDuration,
      sumPeriodRanges: List[PeriodRange],
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorLag: Boolean = false,
      monitorValues: Option[MonitorValuesConfig] = None
  ) extends FeatureConfig {
    val periods: List[Int]   = (sumPeriodRanges.map(_.startOffset) ++ sumPeriodRanges.map(_.endOffset)).sorted
    val latestPeriodOffset   = periods.head
    val earliestPeriodOffset = periods.last
  }

  case class StatsEstimatorConfig(
      ns: Namespace,
      scope: Scope,
      name: FeatureName,
      poolSize: Int,
      sampleRate: Int,
      percentiles: List[Int],
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour,
      monitorLag: Boolean = false,
      monitorValues: Option[MonitorValuesConfig] = None
  ) extends FeatureConfig

  case class ConfigParsingError(msg: String) extends Exception(msg)
  implicit val featureNameDecoder = Decoder.decodeString.map(FeatureName.apply)
  implicit val groupDecoder       = Decoder.decodeString.map(Scope.apply)
  implicit val namespaceDecoder   = Decoder.decodeString.map(Namespace.apply)

  val durationFormat = "([0-9]+)\\s*([a-zA-z]+)".r
  def decodeDuration(str: String) = str match {
    case durationFormat(num, unit) => Try(java.lang.Long.parseLong(num)).map(FiniteDuration.apply(_, unit))
    case _                         => Failure(ConfigParsingError(s"wrong duration format: ${str}"))
  }
  implicit val watchDecoder = deriveDecoder[MonitorValuesConfig]

  implicit val durationDecoder = Decoder.decodeString.emapTry(decodeDuration)

  implicit val statsDecoder         = deriveDecoder[StatsEstimatorConfig]
  implicit val periodicRangeDecoder = deriveDecoder[PeriodRange]
  implicit val periodicDecoder      = deriveDecoder[PeriodicCounterConfig]
  implicit val freqDecoder          = deriveDecoder[FreqEstimatorConfig]
  implicit val listDecoder          = deriveDecoder[BoundedListConfig]
  implicit val scalarDecoder        = deriveDecoder[ScalarConfig]
  implicit val mapDecoder           = deriveDecoder[MapConfig]
  implicit val counterDecoder       = deriveDecoder[CounterConfig]

  implicit val featureDecoder = Decoder.instance[FeatureConfig](c =>
    for {
      tpe <- c.downField("type").as[String]
      decoded <- tpe match {
        case "stats"            => statsDecoder.tryDecode(c)
        case "periodic_counter" => periodicDecoder.tryDecode(c)
        case "frequency"        => freqDecoder.tryDecode(c)
        case "list"             => listDecoder.tryDecode(c)
        case "scalar"           => scalarDecoder.tryDecode(c)
        case "counter"          => counterDecoder.tryDecode(c)
        case "map"              => mapDecoder.tryDecode(c)
        case other              => Left(DecodingFailure(s"feature type $other is not supported", c.history))
      }
    } yield {
      decoded
    }
  )
}
