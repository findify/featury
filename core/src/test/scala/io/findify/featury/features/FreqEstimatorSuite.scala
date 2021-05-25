package io.findify.featury.features

import io.findify.featury.model.Feature.FreqEstimator
import io.findify.featury.model.FeatureConfig.FreqEstimatorConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.{FrequencyValue, Timestamp}
import io.findify.featury.model.Write.PutFreqSample
import io.findify.featury.utils.TestKey

import scala.util.Random

trait FreqEstimatorSuite extends FeatureSuite[PutFreqSample, FrequencyValue] {
  val config: FreqEstimatorConfig =
    FreqEstimatorConfig(FeatureName("f1"), ns = Namespace("a"), group = GroupName("b"), 100, 1)

  it should "sample freqs for 100 items" in {
    val k = TestKey(id = "f10")
    val puts = for { i <- 0 until 100 } yield {
      PutFreqSample(k, Timestamp.now, "p" + math.round(math.abs(Random.nextGaussian() * 10.0)).toString)
    }
    val result = write(puts.toList)
    result.map(_.values.values.sum).get shouldBe 1.0 +- 0.01
    result.map(_.values.getOrElse("p1", 0.0)).get should be > 0.01
  }
}
