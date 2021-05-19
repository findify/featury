package io.findify.featury.feature

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.model.FeatureValue.{ListItem, Scalar, ScalarType, Text}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Timestamp
import io.findify.featury.util.TestKey
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

trait BoundedListSuite[T <: Scalar] extends FixtureAnyFlatSpec with Matchers {
  type FixtureParam = BoundedList[T]

  val config: BoundedListConfig =
    BoundedListConfig(
      name = FeatureName("example"),
      ns = Namespace("a"),
      group = GroupName("b"),
      count = 10,
      duration = 5.hour,
      contentType = contentType
    )

  def contentType: ScalarType
  def makeList(conf: BoundedListConfig): Resource[IO, BoundedList[T]]
  def makeValue(i: Int): T

  override def withFixture(test: OneArgTest): Outcome = {
    val (c, shutdownHandle) = (makeList(config).allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(c))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }

  it should "be empty" in { bl =>
    val key   = TestKey(id = "p10")
    val state = bl.readState(key).unsafeRunSync()
    state shouldBe None
  }

  it should "push elements" in { bl =>
    val now   = Timestamp.now
    val key   = TestKey(id = "p11")
    val value = makeValue(0)
    bl.put(key, value, now).unsafeRunSync()
    val state = bl.readState(key).unsafeRunSync()
    state shouldBe Some(BoundedListState(List(ListItem(value, now))))
  }

  it should "be bounded by element count" in { bl =>
    val now = Timestamp.now
    val key = TestKey(id = "p12")
    val values = for { i <- 0 until 20 } yield {
      bl.put(key, makeValue(i), now).unsafeRunSync()
      ListItem(makeValue(i), now)
    }
    val state        = bl.readState(key).unsafeRunSync().get
    val featureValue = bl.computeValue(state)
    featureValue.map(_.values.size) shouldBe Some(config.count)
  }

  it should "be bounded by time" in { bl =>
    val now = Timestamp.now
    val key = TestKey(id = "p13")
    val values = for { i <- (0 until 10).reverse } yield {
      bl.put(key, makeValue(i), now.minus(i.hours)).unsafeRunSync()
      ListItem(makeValue(i), now.minus(i.hours))
    }
    val state        = bl.readState(key).unsafeRunSync().get
    val featureValue = bl.computeValue(state)
    featureValue.map(_.values.size) shouldBe Some(5)
    val cutoff = now.minus(config.duration)
    featureValue.map(_.values).get.forall(_.ts.isAfter(cutoff)) shouldBe true
  }
}
