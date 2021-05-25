package io.findify.featury.features

import io.findify.featury.model.{Feature, FeatureConfig, Timestamp}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait FeatureSuite[C <: FeatureConfig, F <: Feature[_, _, C]] extends AnyFlatSpec with Matchers {
  def config: C
  def feature: F
  def withFeature(code: F => Any) = {
    val f = feature
    code(f)
  }
  lazy val now = Timestamp.now
}
