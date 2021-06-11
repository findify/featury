package io.findify.featury.features

import io.findify.featury.model.Feature.FreqEstimator
import io.findify.featury.model.FeatureConfig.FreqEstimatorConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.{FrequencyValue, Timestamp}
import io.findify.featury.model.Write.PutFreqSample
import io.findify.featury.utils.TestKey

import scala.util.Random

trait FreqEstimatorSuite extends FeatureSuite[PutFreqSample] {
  val config: FreqEstimatorConfig =
    FreqEstimatorConfig(ns = Namespace("a"), group = Scope("b"), name = FeatureName("f1"), 100, 1)

  it should "sample freqs for 100 items" in {
    val k = TestKey(config, id = "f10")
    val puts = for { i <- 0 until 100 } yield {
      PutFreqSample(k, Timestamp.now, "p" + math.round(math.abs(Random.nextGaussian() * 10.0)).toString)
    }
    val result = write(puts.toList).collect { case f: FrequencyValue => f }.get
    result.values.values.sum shouldBe 1.0 +- 0.001
    result.values.getOrElse("p1", 0.0) should be > 0.01
  }
}
