package io.findify.featury.flink.format

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.syntax._
import io.circe.parser._
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.{FeatureValue, Key, SString, ScalarValue, Timestamp}

class FeatureValueJsonTest extends AnyFlatSpec with Matchers {
  it should "roundtrip scalars" in {
    import io.findify.featury.model.json.FeatureValueJson._
    val value: FeatureValue =
      ScalarValue(Key(Tag(Scope("user"), "u1"), FeatureName("name"), Tenant("default")), Timestamp.now, SString("foo"))
    val json    = value.asJson.noSpaces
    val decoded = decode[FeatureValue](json)
    decoded shouldBe Right(value)
  }
}
