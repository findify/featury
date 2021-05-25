package io.findify.featury.features

import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.{Key, Scalar, ScalarValue, Timestamp}
import io.findify.featury.model.Write.Put
import io.findify.featury.utils.TestKey

trait ScalarFeatureSuite extends FeatureSuite[ScalarConfig, ScalarFeature] {
  def makePut(key: Key, ts: Timestamp, i: Int): Put
  override lazy val config = ScalarConfig(FeatureName("counter"), ns = Namespace("a"), group = GroupName("b"), null)

  it should "read empty" in withFeature { c =>
    c.computeValue(TestKey(), now) shouldBe None
  }

  it should "write and read" in withFeature { c =>
    val key = TestKey(id = "p11")
    val put = makePut(key, now, 1)
    c.put(put)
    c.computeValue(key, now) shouldBe Some(ScalarValue(key, now, put.value))
  }

  it should "update and read" in withFeature { c =>
    val key  = TestKey(id = "p12")
    val put1 = makePut(key, now, 1)
    val put2 = makePut(key, now, 2)
    c.put(put1)
    c.computeValue(key, now) shouldBe Some(ScalarValue(key, now, put1.value))
    c.put(put2)
    c.computeValue(key, now) shouldBe Some(ScalarValue(key, now, put2.value))
  }
}
