package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.model.FeatureValue.{ScalarValue, Text, TextScalarValue}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.ItemFeatures
import io.findify.featury.persistence.ValueStore
import io.findify.featury.persistence.ValueStore.{BatchResult, KeyBatch, KeyFeatures}
import io.findify.featury.util.{TestKey, TestKeyBatch}
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
    val key    = TestKeyBatch(TestKey(id = "p10"))
    val result = v.readBatch(key).unsafeRunSync()
    result.values shouldBe Nil
  }

  it should "write and read" in { v =>
    val key = TestKey(id = "p11", fname = "f1")
    v.write(key, TextScalarValue(Text("foo"))).unsafeRunSync()
    val result = v.readBatch(TestKeyBatch(key)).unsafeRunSync()
    result.values shouldBe List(KeyFeatures(key.id, Map(key.featureName -> TextScalarValue(Text("foo")))))
  }

  it should "write and read into different namespaces" in { v =>
    val key = TestKey(id = "p11", fname = "f1")
    v.write(key, TextScalarValue(Text("foo"))).unsafeRunSync()
    val result = v.readBatch(TestKeyBatch(key)).unsafeRunSync()
    result.values shouldBe List(KeyFeatures(key.id, Map(key.featureName -> TextScalarValue(Text("foo")))))
  }

  it should "update and read" in { v =>
    val key = TestKey(id = "p12")
    v.write(key, TextScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key, TextScalarValue(Text("bar"))).unsafeRunSync()
    val result = v.readBatch(TestKeyBatch(key)).unsafeRunSync()
    result.values shouldBe List(KeyFeatures(key.id, Map(key.featureName -> TextScalarValue(Text("bar")))))
  }

  it should "write and read batch" in { v =>
    val key1 = TestKey(id = "p14")
    val key2 = TestKey(id = "p15")
    v.write(key1, TextScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key2, TextScalarValue(Text("bar"))).unsafeRunSync()
    val result = v
      .readBatch(
        KeyBatch(key1.ns, key1.group, List(key1.featureName, key2.featureName), key1.tenant, List(key1.id, key2.id))
      )
      .unsafeRunSync()
    result.values shouldBe List(
      KeyFeatures(key1.id, Map(key1.featureName -> TextScalarValue(Text("foo")))),
      KeyFeatures(key2.id, Map(key2.featureName -> TextScalarValue(Text("bar"))))
    )
  }
}
