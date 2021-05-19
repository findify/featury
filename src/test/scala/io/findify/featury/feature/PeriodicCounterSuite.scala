package io.findify.featury.feature

import cats.data.NonEmptyList
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.feature.PeriodicCounter.{PeriodRange, PeriodicCounterConfig}
import io.findify.featury.model.FeatureValue.{PeriodicNumValue, PeriodicValue}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Timestamp
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

trait PeriodicCounterSuite extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = PeriodicCounter

  def config: PeriodicCounterConfig =
    PeriodicCounterConfig(
      FeatureName("f1"),
      ns = Namespace("a"),
      group = GroupName("b"),
      1.day,
      10,
      NonEmptyList.of(PeriodRange(0, 0), PeriodRange(7, 0))
    )
  def makeCounter(): Resource[IO, FixtureParam]

  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeCounter().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }

  it should "increment once" in { c =>
    val k   = TestKey(id = "p1")
    val now = Timestamp.now
    c.increment(k, now, 1).unsafeRunSync()
    val result = c.computeValue(c.readState(k).unsafeRunSync().get)
    result shouldBe Some(
      PeriodicNumValue(
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            1.0
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            1.0
          )
        )
      )
    )
  }
  it should "increment once in a intra-day burst" in { c =>
    val k     = TestKey(id = "p2")
    val now   = Timestamp.now
    val start = now.minus(10.hours)
    for {
      offset <- 1 to 10
    } {
      val ts = start.plus(offset.hours)
      c.increment(k, ts, 1).unsafeRunSync()
    }
    val result = c.computeValue(c.readState(k).unsafeRunSync().get)
    result shouldBe Some(
      PeriodicNumValue(
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            10.0
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            10.0
          )
        )
      )
    )
  }

  it should "increment once in a day" in { c =>
    val k     = TestKey(id = "p3")
    val now   = Timestamp.now
    val start = now.minus(10.days)
    for {
      offset <- 1 to 10
    } {
      val ts = start.plus(offset.days)
      c.increment(k, ts, 1).unsafeRunSync()
    }
    val result = c.computeValue(c.readState(k).unsafeRunSync().get)
    result shouldBe Some(
      PeriodicNumValue(
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            1.0
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            8.0
          )
        )
      )
    )
  }

  it should "increment once in a week" in { c =>
    val k     = TestKey(id = "p4")
    val now   = Timestamp.now
    val start = now.minus(10 * 7.days)
    for {
      offset <- 1 to 10
    } {
      val ts = start.plus((7 * offset).days)
      c.increment(k, ts, 1).unsafeRunSync()
    }
    val result = c.computeValue(c.readState(k).unsafeRunSync().get)
    result shouldBe Some(
      PeriodicNumValue(
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            1.0
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            2.0
          )
        )
      )
    )
  }
}
