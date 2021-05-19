package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.model.FeatureValue.{Num, NumScalarValue, ScalarValue}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.{AnyFlatSpec, FixtureAnyFlatSpec}
import org.scalatest.matchers.should.Matchers

import scala.util.Random

trait CounterSuite extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = Counter
  val config = CounterConfig(FeatureName("counter"), ns = Namespace("a"), group = GroupName("b"))
  def makeCounter(): Resource[IO, FixtureParam]
  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeCounter().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }

  it should "increment once" in { counter =>
    val key = TestKey(id = "p10")
    counter.increment(key, 1).unsafeRunSync()
    val state = counter.readState(key).unsafeRunSync()
    state shouldBe Some(CounterState(1))
  }

  it should "inc-dec multiple times" in { counter =>
    val key        = TestKey(id = "p11")
    val increments = (0 until 10).map(_ => Random.nextInt(100) - 50).toList
    increments.foreach(inc => counter.increment(key, inc).unsafeRunSync())
    val state = counter.readState(key).unsafeRunSync()
    state shouldBe Some(CounterState(increments.sum))
  }

  it should "compute value" in { counter =>
    val key = TestKey(id = "p12")
    counter.increment(key, 1).unsafeRunSync()
    val value = counter.computeValue(counter.readState(key).unsafeRunSync().get)
    value shouldBe Some(NumScalarValue(Num(1.0)))
  }

  it should "read zero on empty state" in { counter =>
    val key   = TestKey(id = "p13")
    val state = counter.readState(key).unsafeRunSync()
    state shouldBe None
  }
}
