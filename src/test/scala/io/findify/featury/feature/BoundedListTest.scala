package io.findify.featury.feature

import io.findify.featury.feature.BoundedList.{BoundedListConfig, Push, TextBoundedListFeature}
import io.findify.featury.model.FeatureValue.{BoundedListValue, ListItem, Text}
import io.findify.featury.model.Timestamp
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration._

class BoundedListTest extends AnyFlatSpec with Matchers {
  val now = Timestamp.now
  it should "add items to list" in {
    val conf  = BoundedListConfig()
    val start = TextBoundedListFeature.emptyState(conf)
    val next1 = TextBoundedListFeature.update(conf, start, Push(Text("foo"), now))
    val next2 = TextBoundedListFeature.update(conf, next1, Push(Text("bar"), now))
    val value = TextBoundedListFeature.value(conf, next2)
    value shouldBe BoundedListValue(List(ListItem(Text("bar"), now), ListItem(Text("foo"), now)))
  }
  it should "be bound by size" in {
    val conf  = BoundedListConfig(count = 1)
    val start = TextBoundedListFeature.emptyState(conf)
    val next1 = TextBoundedListFeature.update(conf, start, Push(Text("foo"), now))
    val next2 = TextBoundedListFeature.update(conf, next1, Push(Text("bar"), now))
    val value = TextBoundedListFeature.value(conf, next2)
    value shouldBe BoundedListValue(List(ListItem(Text("bar"), now)))
  }
  it should "be bound by time" in {
    val conf  = BoundedListConfig(duration = 1.minute)
    val start = TextBoundedListFeature.emptyState(conf)
    val next1 = TextBoundedListFeature.update(conf, start, Push(Text("foo"), now))
    val next2 = TextBoundedListFeature.update(conf, next1, Push(Text("bar"), now.plus(1.hour)))
    val value = TextBoundedListFeature.value(conf, next2)
    value shouldBe BoundedListValue(List(ListItem(Text("bar"), now.plus(1.hour))))
  }
}
