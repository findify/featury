package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Feature.State
import io.findify.featury.feature.ScalarFeature.{ScalarConfig, ScalarState}
import io.findify.featury.model.FeatureValue.{Scalar, ScalarValue, Text}
import io.findify.featury.model.Key
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.FeatureConfig

trait ScalarFeature[T <: Scalar] extends Feature[ScalarState[T], ScalarValue[T], ScalarConfig] {
  def put(key: Key, value: T): IO[Unit]
}

object ScalarFeature {
  case class ScalarState[T <: Scalar](value: T)                               extends State
  case class ScalarConfig(ns: Namespace, group: GroupName, name: FeatureName) extends FeatureConfig

//  class TextScalarFeature(val config: ScalarConfig) extends ScalarFeature[Text] {
//    override def empty(): ScalarState[Text] = ScalarState(Text(""))
//  }
}
