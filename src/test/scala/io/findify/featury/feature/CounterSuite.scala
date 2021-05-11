package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.model.FeatureValue.{Num, ScalarValue}
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.{AnyFlatSpec, FixtureAnyFlatSpec}
import org.scalatest.matchers.should.Matchers

import scala.util.Random

trait CounterSuite extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = Counter
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
    counter.increment(key, 1.0).unsafeRunSync()
    val state = counter.readState(key).unsafeRunSync()
    state.value shouldBe 1.0
  }

  it should "inc-dec multiple times" in { counter =>
    val key        = TestKey(id = "p11")
    val increments = (0 until 10).map(_ => Random.nextInt(100) - 50).toList
    increments.foreach(inc => counter.increment(key, inc).unsafeRunSync())
    val state = counter.readState(key).unsafeRunSync()
    state.value shouldBe increments.sum
  }

  it should "compute value" in { counter =>
    val key = TestKey(id = "p12")
    counter.increment(key, 1.0).unsafeRunSync()
    val value = counter.computeValue(counter.readState(key).unsafeRunSync())
    value shouldBe ScalarValue(Num(1.0))
  }

  it should "read zero on empty state" in { counter =>
    val key   = TestKey(id = "p13")
    val value = counter.computeValue(counter.readState(key).unsafeRunSync())
    value shouldBe ScalarValue(Num(0.0))
  }
}
