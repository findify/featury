package io.findify.featury.features

import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.FeatureValue.ScalarValue
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Timestamp
import io.findify.featury.model.Write.Increment
import io.findify.featury.utils.TestKey

import scala.util.Random

trait CounterSuite extends FeatureSuite[CounterConfig, Counter] {
  override lazy val config = CounterConfig(FeatureName("c1"), Namespace("n1"), GroupName("g1"))

  it should "increment once" in withFeature { counter =>
    val key = TestKey(id = "p10")
    counter.put(Increment(key, Timestamp.now, 1))
    counter.computeValue(key) shouldBe Some(ScalarValue(1))
  }

  it should "inc-dec multiple times" in withFeature { counter =>
    val key        = TestKey(id = "p11")
    val increments = (0 until 10).map(_ => Random.nextInt(100) - 50).toList
    increments.foreach(inc => counter.put(Increment(key, Timestamp.now, inc)))
    counter.computeValue(key) shouldBe Some(ScalarValue(increments.sum))
  }

  it should "read zero on empty state" in withFeature { counter =>
    val key   = TestKey(id = "p13")
    val state = counter.computeValue(key)
    state shouldBe None
  }
}
