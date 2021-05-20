package io.findify.featury.features

import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.FeatureValue.{Scalar, ScalarValue}
import io.findify.featury.model.Key._
import io.findify.featury.model.WriteRequest.Put
import io.findify.featury.utils.TestKey

trait ScalarFeatureSuite[T <: Scalar] extends FeatureSuite[ScalarConfig, ScalarFeature[T]] {
  def makeValue(i: Int): T
  override lazy val config = ScalarConfig(FeatureName("counter"), ns = Namespace("a"), group = GroupName("b"), null)

  it should "read empty" in withFeature { c =>
    c.computeValue(TestKey()) shouldBe None
  }

  it should "write and read" in withFeature { c =>
    val key   = TestKey(id = "p11")
    val value = makeValue(1)
    c.put(Put(key, value))
    c.computeValue(key) shouldBe Some(ScalarValue(value))
  }

  it should "update and read" in withFeature { c =>
    val key    = TestKey(id = "p12")
    val value1 = makeValue(1)
    val value2 = makeValue(2)
    c.put(Put(key, value1))
    c.computeValue(key) shouldBe Some(ScalarValue(value1))
    c.put(Put(key, value2))
    c.computeValue(key) shouldBe Some(ScalarValue(value2))
  }
}
