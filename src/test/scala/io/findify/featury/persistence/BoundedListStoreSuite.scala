package io.findify.featury.persistence

import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState, Push, TextBoundedListFeature}
import io.findify.featury.feature.Counter.{CounterConfig, CounterFeature, CounterState, Increment}
import io.findify.featury.model.FeatureValue.{ListItem, Text}
import io.findify.featury.model.{Key, Timestamp}
import io.findify.featury.persistence.StateStore.StateDelta
import cats.effect.unsafe.implicits.global

trait BoundedListStoreSuite[T <: StateStore[BoundedListState[Text]]] extends StoreSuite[BoundedListState[Text], T] {
  val k    = Key("product", "count", 1, "foo")
  val conf = BoundedListConfig()

  it should "w+w+w and read" in { fixture =>
    val now = Timestamp.now
    List("foo", "bar").foldLeft(TextBoundedListFeature.emptyState(conf)) {
      case (state, text) => {
        val updated = TextBoundedListFeature.update(conf, state, Push(Text(text), now))
        fixture.p.write(List(k -> StateDelta(updated))).unsafeRunSync()
        updated
      }
    }
    val result = fixture.p.read(List(k)).unsafeRunSync()
    result shouldBe Map(k -> BoundedListState[Text](List(ListItem(Text("bar"), now), ListItem(Text("foo"), now))))
  }

  it should "write+read in a loop" in { fixture =>
    val now = Timestamp.now
    List("foo", "bar", "baz").foreach(inc => {
      val state   = fixture.p.read(List(k)).unsafeRunSync().getOrElse(k, TextBoundedListFeature.emptyState(conf))
      val updated = TextBoundedListFeature.update(conf, state, Push(Text(inc), now))
      fixture.p.write(List(k -> StateDelta(updated))).unsafeRunSync()
    })
    val result = fixture.p.read(List(k)).unsafeRunSync()
    result shouldBe Map(
      k -> BoundedListState[Text](
        List(
          ListItem(Text("baz"), now),
          ListItem(Text("bar"), now),
          ListItem(Text("foo"), now)
        )
      )
    )
  }

}
