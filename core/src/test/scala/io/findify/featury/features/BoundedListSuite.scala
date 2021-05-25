package io.findify.featury.features

import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.{Scalar, Timestamp}
import io.findify.featury.model.Write.Append
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
    bl.computeValue(key, now) shouldBe None
  }

  it should "push elements" in withFeature { bl =>
    val key   = TestKey(id = "p11")
    val value = makeValue(0)
    bl.put(Append(key, value, now))
    bl.computeValue(key, now).flatMap(_.value.headOption.map(_.value)) shouldBe Some(value)
  }

  it should "be bounded by element count" in withFeature { bl =>
    val key = TestKey(id = "p12")
    for { i <- 0 until 20 } { bl.put(Append(key, makeValue(i), now)) }
    val featureValue = bl.computeValue(key, now)
    featureValue.map(_.value.size) shouldBe Some(config.count)
  }

  it should "be bounded by time" in withFeature { bl =>
    val key = TestKey(id = "p13")
    for { i <- (0 until 10).reverse } { bl.put(Append(key, makeValue(i), now.minus(i.hours))) }
    val featureValue = bl.computeValue(key, now)
    val cutoff       = now.minus(config.duration)
    featureValue.map(_.value.forall(_.ts.isAfterOrEquals(cutoff))) shouldBe Some(true)
  }
}
