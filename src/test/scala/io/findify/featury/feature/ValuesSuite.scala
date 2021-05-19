package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.model.FeatureValue.{ScalarValue, Text}
import io.findify.featury.model.Key.{FeatureName, Namespace}
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
    v.write(key, ScalarValue(Text("foo"))).unsafeRunSync()
    val result = v.readBatch(TestKeyBatch(key)).unsafeRunSync()
    result.values shouldBe List(KeyFeatures(key.id, Map(key.featureName -> ScalarValue(Text("foo")))))
  }

  it should "write and read into different namespaces" in { v =>
    val key1 = TestKey(id = "p11", fname = "f1").copy(ns = Namespace("n1"))
    val key2 = TestKey(id = "p11", fname = "f1").copy(ns = Namespace("n2"))
    v.write(key1, ScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key2, ScalarValue(Text("bar"))).unsafeRunSync()
    val result1 = v.readBatch(TestKeyBatch(key1)).unsafeRunSync()
    result1.values shouldBe List(KeyFeatures(key1.id, Map(key1.featureName -> ScalarValue(Text("foo")))))
    val result2 = v.readBatch(TestKeyBatch(key2)).unsafeRunSync()
    result2.values shouldBe List(KeyFeatures(key2.id, Map(key2.featureName -> ScalarValue(Text("bar")))))
  }

  it should "update and read" in { v =>
    val key = TestKey(id = "p12")
    v.write(key, ScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key, ScalarValue(Text("bar"))).unsafeRunSync()
    val result = v.readBatch(TestKeyBatch(key)).unsafeRunSync()
    result.values shouldBe List(KeyFeatures(key.id, Map(key.featureName -> ScalarValue(Text("bar")))))
  }

  it should "write and read batch" in { v =>
    val key1 = TestKey(id = "p14")
    val key2 = TestKey(id = "p15")
    v.write(key1, ScalarValue(Text("foo"))).unsafeRunSync()
    v.write(key2, ScalarValue(Text("bar"))).unsafeRunSync()
    val result = v
      .readBatch(
        KeyBatch(key1.ns, key1.group, List(key1.featureName, key2.featureName), key1.tenant, List(key1.id, key2.id))
      )
      .unsafeRunSync()
    result.values shouldBe List(
      KeyFeatures(key1.id, Map(key1.featureName -> ScalarValue(Text("foo")))),
      KeyFeatures(key2.id, Map(key2.featureName -> ScalarValue(Text("bar"))))
    )
  }
}
