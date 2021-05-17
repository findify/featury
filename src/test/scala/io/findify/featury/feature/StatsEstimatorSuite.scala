package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.feature.StatsEstimator.StatsEstimatorConfig
import io.findify.featury.model.FeatureValue.NumStatsValue
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait StatsEstimatorSuite extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = StatsEstimator

  def config: StatsEstimatorConfig = StatsEstimatorConfig(FeatureName("f1"), 100, 1, List(50, 90))
  def makeCounter(): Resource[IO, FixtureParam]

  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeCounter().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }

  it should "measure a 1-100 range" in { s =>
    val k = TestKey()
    for { i <- 0 until 100 } { s.put(k, i.toDouble).unsafeRunSync() }
    val result = s.computeValue(s.readState(k).unsafeRunSync())
    result shouldBe Some(
      NumStatsValue(
        min = 0.0,
        max = 99.0,
        quantiles = Map(50 -> 49.5, 90 -> 89.1)
      )
    )
  }

  it should "measure a 1-1000 range" in { s =>
    val k = TestKey(id = "p2")
    for { i <- 0 until 1000 } { s.put(k, i.toDouble).unsafeRunSync() }
    val result = s.computeValue(s.readState(k).unsafeRunSync()).get
    result.min should be > 100.0
    result.max should be > 100.0
    result.quantiles.values.toList.forall(_ > 100) shouldBe true
  }
}
