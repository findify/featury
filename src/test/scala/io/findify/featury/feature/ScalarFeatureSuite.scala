package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.ScalarFeature.{ScalarConfig, ScalarState}
import io.findify.featury.model.FeatureValue.Scalar
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait ScalarFeatureSuite[T <: Scalar] extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = ScalarFeature[T]
  val config = ScalarConfig(FeatureName("counter"), ns = Namespace("a"), group = GroupName("b"))
  def makeValue: T
  def makeCounter(): Resource[IO, FixtureParam]
  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeCounter().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }

  it should "read empty" in { c =>
    c.readState(TestKey()).unsafeRunSync() shouldBe None
  }

  it should "write and read" in { c =>
    val key   = TestKey(id = "p11")
    val value = makeValue
    c.put(key, value).unsafeRunSync()
    c.readState(key).unsafeRunSync() shouldBe Some(ScalarState(value))
  }

  it should "update and read" in { c =>
    val key    = TestKey(id = "p12")
    val value1 = makeValue
    val value2 = makeValue
    c.put(key, value1).unsafeRunSync()
    c.readState(key).unsafeRunSync() shouldBe Some(ScalarState(value1))
    c.put(key, value2).unsafeRunSync()
    c.readState(key).unsafeRunSync() shouldBe Some(ScalarState(value2))
  }
}
