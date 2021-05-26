package io.findify.featury.features

import io.findify.featury.model.Feature.StatsEstimator
import io.findify.featury.model.FeatureConfig.StatsEstimatorConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.{NumStatsValue, Timestamp}
import io.findify.featury.model.Write.PutStatSample
import io.findify.featury.utils.TestKey

trait StatsEstimatorSuite extends FeatureSuite[PutStatSample, NumStatsValue] {
  val config =
    StatsEstimatorConfig(FeatureName("f1"), ns = Namespace("a"), group = GroupName("b"), 100, 1, List(50, 90))

  it should "measure a 1-100 range" in {
    val k      = TestKey(config, id = "p10")
    val puts   = for { i <- 0 until 100 } yield { PutStatSample(k, now, i.toDouble) }
    val result = write(puts.toList).get
    result.min should be >= 0.0
    result.max should be <= 100.0
    result.quantiles.values.toList.forall(_ > 1) shouldBe true
  }

  it should "measure a 1-1000 range" in {
    val k      = TestKey(config, id = "p11")
    val puts   = for { i <- 0 until 1000 } yield { PutStatSample(k, now, i.toDouble) }
    val result = write(puts.toList).get
    result.quantiles.values.toList.exists(_ > 10) shouldBe true
  }
}
