package io.findify.featury.model

import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.model.Schema.FeatureType.{BoundedListType, CounterType}
import io.findify.featury.model.Schema.{FeatureConfig, FeatureSchema, GroupSchema}

case class Schema(groups: List[GroupSchema]) {
  val counterConfigs = groups
    .flatMap(g =>
      g.features.flatMap {
        case Schema.CounterFeatureSchema(name, config) => Some(FeatureName(g.name, name) -> config)
        case _                                         => None
      }
    )
    .toMap
}

object Schema {
  case class GroupSchema(name: String, features: List[FeatureSchema])

  sealed trait FeatureSchema {
    def name: String
    def tpe: FeatureType
  }

  case class CounterFeatureSchema(name: String, config: CounterConfig) extends FeatureSchema {
    override val tpe = CounterType
  }

  case class BoundedListConfig(name: String, tpe: BoundedListType, config: BoundedListConfig) extends FeatureSchema

  sealed trait FeatureType
  object FeatureType {
    case object TextType         extends FeatureType
    case object NumType          extends FeatureType
    case object CounterType      extends FeatureType
    sealed trait BoundedListType extends FeatureType
    case object TextListType     extends BoundedListType
    case object NumListType      extends BoundedListType
  }

  trait FeatureConfig
}
