package io.findify.featury.features

import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.{Key, SString, Scalar, ScalarValue, Timestamp}
import io.findify.featury.model.Write.Put
import io.findify.featury.utils.TestKey

trait ScalarFeatureSuite extends FeatureSuite[Put, ScalarValue] {
  val config = ScalarConfig(FeatureName("counter"), ns = Namespace("a"), group = GroupName("b"), null)

  it should "write and read" in {
    val key    = TestKey(config, id = "p11")
    val result = write(List(Put(key, now, SString("foo"))))
    result shouldBe Some(ScalarValue(key, now, SString("foo")))
  }

  it should "update and read" in {
    val key    = TestKey(config, id = "p12")
    val put1   = Put(key, now, SString("1"))
    val put2   = Put(key, now, SString("2"))
    val result = write(List(put1, put2))
    result shouldBe Some(ScalarValue(key, now, put2.value))
  }
}
