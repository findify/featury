package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.model.FeatureValue.{ScalarValue, Text}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.persistence.ValueStore
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait ValuesSuite extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = ValueStore
  def makeValues(): Resource[IO, ValueStore]
  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeValues().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }

  it should "read empty" in { v =>
    val key    = TestKey(id = "p10")
    val result = v.readBatch(List(key)).unsafeRunSync()
    result shouldBe Map(key -> Map.empty)
  }

  it should "write and read" in { v =>
    val key = TestKey(id = "p11")
    v.write(key, FeatureName("f1"), ScalarValue(Text("foo"))).unsafeRunSync()
    val result = v.readBatch(List(key)).unsafeRunSync()
    result shouldBe Map(key -> Map(FeatureName("f1") -> ScalarValue(Text("foo"))))
  }

  it should "update and read" in { v =>
    val key = TestKey(id = "p12")
    v.write(key, FeatureName("f1"), ScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key, FeatureName("f1"), ScalarValue(Text("bar"))).unsafeRunSync()
    val result = v.readBatch(List(key)).unsafeRunSync()
    result shouldBe Map(key -> Map(FeatureName("f1") -> ScalarValue(Text("bar"))))
  }

  it should "append and read" in { v =>
    val key = TestKey(id = "p13")
    v.write(key, FeatureName("f1"), ScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key, FeatureName("f2"), ScalarValue(Text("bar"))).unsafeRunSync()
    val result = v.readBatch(List(key)).unsafeRunSync()
    result shouldBe Map(
      key -> Map(FeatureName("f1") -> ScalarValue(Text("foo")), FeatureName("f2") -> ScalarValue(Text("bar")))
    )
  }

  it should "write and read batch" in { v =>
    val key1 = TestKey(id = "p14")
    val key2 = TestKey(id = "p15")
    v.write(key1, FeatureName("f1"), ScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key2, FeatureName("f2"), ScalarValue(Text("bar"))).unsafeRunSync()
    val result = v.readBatch(List(key1, key2)).unsafeRunSync()
    result shouldBe Map(
      key1 -> Map(FeatureName("f1") -> ScalarValue(Text("foo"))),
      key2 -> Map(FeatureName("f2") -> ScalarValue(Text("bar")))
    )
  }
}
