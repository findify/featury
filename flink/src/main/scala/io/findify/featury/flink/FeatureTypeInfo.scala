package io.findify.featury.flink

import io.findify.featury.model.{FeatureValue, Scalar, Write}
import io.findify.flinkadt.api._

object FeatureTypeInfo {
  implicit val scalar       = deriveTypeInformation[Scalar]
  implicit val write        = deriveTypeInformation[Write]
  implicit val featureValue = deriveTypeInformation[FeatureValue]
}
