package io.findify.featury.features

import io.findify.featury.model.{Feature, FeatureConfig, FeatureValue, State, Timestamp, Write}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait FeatureSuite[W <: Write] extends AnyFlatSpec with Matchers {
  lazy val now = Timestamp.now

  def write(values: List[W]): Option[FeatureValue]

  it should "be empty" in {
    write(Nil) shouldBe None
  }

}
