package io.findify.featury.model

import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.yaml.parser
import io.findify.featury.model.FeatureConfig._
import io.findify.featury.model.Key._

case class Schema(
    counters: Map[FeatureKey, CounterConfig],
    scalars: Map[FeatureKey, ScalarConfig],
    periodicCounters: Map[FeatureKey, PeriodicCounterConfig],
    freqs: Map[FeatureKey, FreqEstimatorConfig],
    stats: Map[FeatureKey, StatsEstimatorConfig],
    lists: Map[FeatureKey, BoundedListConfig],
    maps: Map[FeatureKey, MapConfig],
    configs: Map[FeatureKey, FeatureConfig]
) {
  val scopeNameCache: Map[Scope, List[FeatureName]] =
    configs.values.toList.groupBy(_.scope).map { case (k, configs) =>
      k -> configs.map(_.name)
    }
}

object Schema {
  case class SchemaYaml(features: List[FeatureConfig])

  def fromYaml(text: String): Either[ConfigParsingError, Schema] = {
    parser.parse(text) match {
      case Left(err) => Left(ConfigParsingError(s"cannot decode yaml: $err"))
      case Right(yaml) =>
        yaml.as[Schema] match {
          case Left(err)     => Left(ConfigParsingError(s"cannot decode yaml: $err"))
          case Right(schema) => Right(schema)
        }
    }
  }
  def apply(conf: FeatureConfig): Schema = apply(List(conf))
  def apply(confs: List[FeatureConfig]): Schema = {
    val configs = for {
      c <- confs
    } yield {
      FeatureKey(c.scope, c.name) -> c
    }
    new Schema(
      counters = configs.collect { case (key, c: CounterConfig) => key -> c }.toMap,
      scalars = configs.collect { case (key, c: ScalarConfig) => key -> c }.toMap,
      periodicCounters = configs.collect { case (key, c: PeriodicCounterConfig) => key -> c }.toMap,
      freqs = configs.collect { case (key, c: FreqEstimatorConfig) => key -> c }.toMap,
      stats = configs.collect { case (key, c: StatsEstimatorConfig) => key -> c }.toMap,
      lists = configs.collect { case (key, c: BoundedListConfig) => key -> c }.toMap,
      maps = configs.collect { case (key, c: MapConfig) => key -> c }.toMap,
      configs = configs.toMap
    )
  }
  implicit val schemaDecoder: Decoder[Schema] = deriveDecoder[SchemaYaml].map(s => Schema(s.features))
}
