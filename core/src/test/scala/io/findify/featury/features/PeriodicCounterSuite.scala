package io.findify.featury.features

import io.findify.featury.model.Feature.PeriodicCounter
import io.findify.featury.model.FeatureConfig.{PeriodRange, PeriodicCounterConfig}
import io.findify.featury.model.Key._
import io.findify.featury.model.PeriodicCounterValue.PeriodicValue
import io.findify.featury.model.{PeriodicCounterValue, Timestamp}
import io.findify.featury.model.Write.PeriodicIncrement
import io.findify.featury.utils.TestKey

import scala.concurrent.duration._

trait PeriodicCounterSuite extends FeatureSuite[PeriodicCounterConfig, PeriodicCounter] {
  override val config = PeriodicCounterConfig(
    FeatureName("f1"),
    ns = Namespace("a"),
    group = GroupName("b"),
    1.day,
    10,
    List(PeriodRange(0, 0), PeriodRange(7, 0))
  )

  it should "increment once" in withFeature { c =>
    val k   = TestKey(id = "p1")
    val now = Timestamp.now
    c.put(PeriodicIncrement(k, now, 1))
    val result = c.computeValue(k)
    result shouldBe Some(
      PeriodicCounterValue(
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
  it should "increment once in a intra-day burst" in withFeature { c =>
    val k     = TestKey(id = "p2")
    val now   = Timestamp.now
    val start = now.minus(10.hours)
    for {
      offset <- 1 to 10
    } {
      val ts = start.plus(offset.hours)
      c.put(PeriodicIncrement(k, ts, 1))
    }
    val result = c.computeValue(k)
    result shouldBe Some(
      PeriodicCounterValue(
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

  it should "increment once in a day" in withFeature { c =>
    val k     = TestKey(id = "p3")
    val now   = Timestamp.now
    val start = now.minus(10.days)
    for {
      offset <- 1 to 10
    } {
      val ts = start.plus(offset.days)
      c.put(PeriodicIncrement(k, ts, 1))
    }
    val result = c.computeValue(k)
    result shouldBe Some(
      PeriodicCounterValue(
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

  it should "increment once in a week" in withFeature { c =>
    val k     = TestKey(id = "p4")
    val now   = Timestamp.now
    val start = now.minus(10 * 7.days)
    for {
      offset <- 1 to 10
    } {
      val ts = start.plus((7 * offset).days)
      c.put(PeriodicIncrement(k, ts, 1))
    }
    val result = c.computeValue(k)
    result shouldBe Some(
      PeriodicCounterValue(
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
