package io.findify.featury.persistence

import io.findify.featury.feature.Counter.{CounterConfig, CounterFeature, CounterState, Increment}
import io.findify.featury.model.{Key, Timestamp}
import io.findify.featury.persistence.StateStore.StateDelta
import cats.effect.unsafe.implicits.global

trait CounterStoreSuite[T <: StateStore[CounterState]] extends StoreSuite[CounterState, T] {

  val k    = Key("product", "count", 1, "foo")
  val conf = CounterConfig()

  it should "w+w+w and read" in { fixture =>
    List(1, 3, -1).foldLeft(CounterFeature.emptyState(conf)) {
      case (state, inc) => {
        val updated = CounterFeature.update(conf, state, Increment(inc, Timestamp.now))
        fixture.p.write(List(k -> StateDelta(updated))).unsafeRunSync()
        updated
      }
    }
    val result = fixture.p.read(List(k)).unsafeRunSync()
    result shouldBe Map(k -> CounterState(3))
  }

  it should "write+read in a loop" in { fixture =>
    List(1, 3, -1).foreach(inc => {
      val state   = fixture.p.read(List(k)).unsafeRunSync().getOrElse(k, CounterFeature.emptyState(conf))
      val updated = CounterFeature.update(conf, state, Increment(inc, Timestamp.now))
      fixture.p.write(List(k -> StateDelta(updated))).unsafeRunSync()
    })
    val result = fixture.p.read(List(k)).unsafeRunSync()
    result shouldBe Map(k -> CounterState(3))
  }

}
