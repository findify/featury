package io.findify.featury.features

import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model.FeatureValue.{BoundedListValue, ListItem, Scalar, ScalarType}
import io.findify.featury.model.Key._
import io.findify.featury.model.Timestamp
import io.findify.featury.model.WriteRequest.Append
import io.findify.featury.utils.TestKey

import scala.concurrent.duration._

trait BoundedListSuite[T <: Scalar] extends FeatureSuite[BoundedListConfig, BoundedList[T]] {
  def makeValue(i: Int): T
  override lazy val config = BoundedListConfig(
    name = FeatureName("example"),
    ns = Namespace("a"),
    group = GroupName("b"),
    count = 10,
    duration = 5.hour,
    contentType = null
  )
  it should "be empty" in withFeature { bl =>
    val key = TestKey(id = "p10")
    bl.computeValue(key) shouldBe None
  }

  it should "push elements" in withFeature { bl =>
    val now   = Timestamp.now
    val key   = TestKey(id = "p11")
    val value = makeValue(0)
    bl.put(Append(key, value, now))
    bl.computeValue(key) shouldBe Some(BoundedListValue(List(ListItem(value, now))))
  }

  it should "be bounded by element count" in withFeature { bl =>
    val now = Timestamp.now
    val key = TestKey(id = "p12")
    for { i <- 0 until 20 } { bl.put(Append(key, makeValue(i), now)) }
    val featureValue = bl.computeValue(key)
    featureValue.map(_.value.size) shouldBe Some(config.count)
  }

  it should "be bounded by time" in withFeature { bl =>
    val now = Timestamp.now
    val key = TestKey(id = "p13")
    for { i <- (0 until 10).reverse } { bl.put(Append(key, makeValue(i), now.minus(i.hours))) }
    val featureValue = bl.computeValue(key)
    val cutoff       = now.minus(config.duration)
    featureValue.map(_.value.forall(_.ts.isAfterOrEquals(cutoff))) shouldBe Some(true)
  }
}
