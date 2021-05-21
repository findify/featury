package io.findify.featury.features

import io.findify.featury.model.Feature.StatsEstimator
import io.findify.featury.model.FeatureConfig.StatsEstimatorConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.Timestamp
import io.findify.featury.model.Write.PutStatSample
import io.findify.featury.utils.TestKey

trait StatsEstimatorSuite extends FeatureSuite[StatsEstimatorConfig, StatsEstimator] {
  override val config =
    StatsEstimatorConfig(FeatureName("f1"), ns = Namespace("a"), group = GroupName("b"), 100, 1, List(50, 90))

  it should "measure a 1-100 range" in withFeature { s =>
    val k = TestKey(id = "p10")
    for { i <- 0 until 100 } { s.put(PutStatSample(k, Timestamp.now, i.toDouble)) }
    val result = s.computeValue(k).get
    result.min should be >= 0.0
    result.max should be <= 100.0
    result.quantiles.values.toList.forall(_ > 1) shouldBe true
  }

  it should "measure a 1-1000 range" in withFeature { s =>
    val k = TestKey(id = "p11")
    for { i <- 0 until 1000 } { s.put(PutStatSample(k, Timestamp.now, i.toDouble)) }
    val result = s.computeValue(k).get
    result.min should be > 100.0
    result.max should be > 100.0
    result.quantiles.values.toList.forall(_ > 10) shouldBe true
  }
}
