package io.findify.featury

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.model.Key.{FeatureName, Tag}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{SString, ScalarValue, Timestamp}
import io.findify.featury.utils.TestKey
import io.findify.featury.values.FeatureStore
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration._

trait StoreTestSuite extends AnyFlatSpec with BeforeAndAfterAll with Matchers { this: Suite =>
  def storeResource: Resource[IO, FeatureStore]
  private var store: FeatureStore = _
  private var shutdown: IO[Unit]  = _
  val now                         = Timestamp.now

  override def beforeAll(): Unit = {
    super.beforeAll()
    val pair = storeResource.allocated.unsafeRunSync()
    store = pair._1
    shutdown = pair._2
  }

  override def afterAll(): Unit = {
    shutdown.unsafeRunSync()
    super.afterAll()
  }

  it should "write/read to the store" in {
    val k     = TestKey(fname = "title", id = "p")
    val value = ScalarValue(k, now, SString("foo"))
    store.write(List(value))
    val result = store.read(ReadRequest(List(k.tag), k.tenant, List(k.name))).unsafeRunSync()
    result shouldBe ReadResponse(List(value))
  }

  it should "append features" in {
    val k      = TestKey(id = "p10")
    val value1 = ScalarValue(k.copy(name = FeatureName("foo1")), now, SString("foo"))
    store.write(List(value1))
    val value2 = ScalarValue(k.copy(name = FeatureName("foo2")), now, SString("foo"))
    store.write(List(value2))
    val result = store
      .read(ReadRequest(List(k.tag), k.tenant, List(value1.key.name, value2.key.name)))
      .unsafeRunSync()
    result.features.toSet shouldBe Set(value2, value1)
  }

  it should "overwrite values" in {
    val k      = TestKey(id = "p10")
    val value1 = ScalarValue(k.copy(name = FeatureName("foo1")), now, SString("foo"))
    store.write(List(value1))
    val value2 = ScalarValue(k.copy(name = FeatureName("foo1")), now.plus(10.seconds), SString("foo"))
    store.write(List(value2))
    val result = store.read(ReadRequest(List(k.tag), k.tenant, List(value1.key.name))).unsafeRunSync()
    result shouldBe ReadResponse(List(value2))
  }

  it should "read non-existing keys" in {
    val k     = TestKey(fname = "title", id = "p11")
    val value = ScalarValue(k, now, SString("foo"))
    store.write(List(value))
    val result = store.read(ReadRequest(List(k.tag), k.tenant, List(k.name))).unsafeRunSync()
    result shouldBe ReadResponse(Nil)
  }

  it should "read non-existing features" in {
    val k     = TestKey(fname = "title", id = "p11")
    val value = ScalarValue(k, now, SString("foo"))
    store.write(List(value))
    val result =
      store.read(ReadRequest(List(k.tag), k.tenant, List(FeatureName("non-existent")))).unsafeRunSync()
    result shouldBe ReadResponse(Nil)

  }
}
