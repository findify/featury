package io.findify.featury.features

import io.findify.featury.model.Feature.PeriodicCounter
import io.findify.featury.model.FeatureConfig.{PeriodRange, PeriodicCounterConfig}
import io.findify.featury.model.Key._
import io.findify.featury.model.PeriodicCounterValue.PeriodicValue
import io.findify.featury.model.{PeriodicCounterValue, Timestamp}
import io.findify.featury.model.Write.PeriodicIncrement
import io.findify.featury.utils.TestKey

import scala.concurrent.duration._

trait PeriodicCounterSuite extends FeatureSuite[PeriodicIncrement] {
  val config = PeriodicCounterConfig(
    ns = Namespace("a"),
    group = Scope("b"),
    FeatureName("f1"),
    1.day,
    List(PeriodRange(0, 0), PeriodRange(7, 0))
  )

  it should "increment once" in {
    val k      = TestKey(config, id = "p1")
    val result = write(List(PeriodicIncrement(k, now, 1)))
    result shouldBe Some(
      PeriodicCounterValue(
        k,
        now,
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            1
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            1
          )
        )
      )
    )
  }
  it should "increment once in a intra-day burst" in {
    val k     = TestKey(config, id = "p2")
    val start = now.minus(10.hours)
    val incrs = for {
      offset <- 1 to 10
    } yield {
      val ts = start.plus(offset.hours)
      PeriodicIncrement(k, ts, 1)
    }
    val result = write(incrs.toList)
    result shouldBe Some(
      PeriodicCounterValue(
        k,
        now,
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            10
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            10
          )
        )
      )
    )
  }

  it should "increment once in a day" in {
    val k     = TestKey(config, id = "p3")
    val start = now.minus(10.days)
    val incrs = for {
      offset <- 1 to 10
    } yield {
      val ts = start.plus(offset.days)
      PeriodicIncrement(k, ts, 1)
    }
    val result = write(incrs.toList)
    result shouldBe Some(
      PeriodicCounterValue(
        k,
        now,
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            1
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            8
          )
        )
      )
    )
  }

  it should "increment once in a week" in {
    val k     = TestKey(config, id = "p4")
    val start = now.minus(10 * 7.days)
    val incrs = for {
      offset <- 1 to 10
    } yield {
      val ts = start.plus((7 * offset).days)
      PeriodicIncrement(k, ts, 1)
    }
    val result = write(incrs.toList)
    result shouldBe Some(
      PeriodicCounterValue(
        k,
        now,
        List(
          PeriodicValue(
            now.toStartOfPeriod(config.period),
            now.toStartOfPeriod(config.period).plus(config.period),
            1,
            1
          ),
          PeriodicValue(
            now.toStartOfPeriod(config.period).minus(config.period * 7),
            now.toStartOfPeriod(config.period).plus(config.period),
            8,
            2
          )
        )
      )
    )
  }
}
