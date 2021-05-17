package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.feature.FreqEstimator.FreqEstimatorConfig
import io.findify.featury.feature.StatsEstimator.StatsEstimatorConfig
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

trait FreqEstimatorSuite extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = FreqEstimator

  def config: FreqEstimatorConfig =
    FreqEstimatorConfig(FeatureName("f1"), ns = Namespace("a"), group = GroupName("b"), 100, 1)
  def makeCounter(): Resource[IO, FixtureParam]

  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeCounter().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }
  it should "sample freqs for 100 items" in { s =>
    val k = TestKey()
    for { i <- 0 until 100 } {
      s.put(k, "p" + math.round(math.abs(Random.nextGaussian() * 10.0)).toString).unsafeRunSync()
    }
    val result = s.computeValue(s.readState(k).unsafeRunSync()).get
    result.values.values.sum shouldBe 1.0 +- 0.01
    result.values.getOrElse("p1", 0.0) should be > 0.01
  }
}
