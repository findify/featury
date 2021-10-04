package io.findify.featury.features

import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model.Key._
import io.findify.featury.model.ScalarType.TextType
import io.findify.featury.model.{BoundedListState, BoundedListValue, SString, Scalar, TimeValue, Timestamp}
import io.findify.featury.model.Write.{Append, Put}
import io.findify.featury.utils.TestKey

import scala.concurrent.duration._

trait BoundedListSuite extends FeatureSuite[Append] {
  val config = BoundedListConfig(
    name = FeatureName("example"),
    scope = Scope("b"),
    count = 10,
    duration = 5.hour
  )
  it should "push single element" in {
    val key    = TestKey(config, id = "p11")
    val result = write(List(Append(key, SString("foo"), now)))
    result should matchPattern {
      case Some(BoundedListValue(_, _, vals)) if vals == List(TimeValue(now, SString("foo"))) =>
    }
  }

  it should "push multiple elements" in {
    val key    = TestKey(config, id = "p12")
    val result = write(List(Append(key, SString("foo"), now), Append(key, SString("bar"), now)))
    result should matchPattern {
      case Some(BoundedListValue(_, _, value))
          if value == List(TimeValue(now, SString("bar")), TimeValue(now, SString("foo"))) =>
    }
  }

  it should "be bounded by element count" in {
    val key     = TestKey(config, id = "p12")
    val appends = for { i <- 0 until 20 } yield { Append(key, SString(i.toString), now) }
    val result  = write(appends.toList)
    result should matchPattern {
      case Some(BoundedListValue(_, _, values)) if values.size == config.count =>
    }
  }

  it should "be bounded by time" in {
    val key     = TestKey(config, id = "p13")
    val appends = for { i <- (0 until 10).reverse } yield { Append(key, SString(i.toString), now.minus(i.hours)) }
    val result  = write(appends.toList)
    val cutoff  = now.minus(config.duration)
    result should matchPattern {
      case Some(BoundedListValue(_, _, values)) if values.forall(_.ts.isAfterOrEquals(cutoff)) =>
    }
  }
}
