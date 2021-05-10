package io.findify.featury.persistence

import io.findify.featury.feature.BoundedList.BoundedListState
import io.findify.featury.feature.Counter.CounterState
import io.findify.featury.model.Feature.State
import io.findify.featury.model.FeatureValue.{Num, Text}
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.persistence.StateStore.StateDelta
import cats.effect.IO

trait StateStore[S <: State] {
  def read(keys: List[Key]): IO[Map[Key, S]]
  def write(keys: List[(Key, StateDelta[S])]): IO[Unit]
}

object StateStore {
  type CounterStore  = StateStore[CounterState]
  type TextListStore = StateStore[BoundedListState[Text]]
  type NumListStore  = StateStore[BoundedListState[Num]]

  case class StateDelta[S <: State](before: S, after: S)
  object StateDelta {
    def apply[S <: State](same: S) = new StateDelta(same, same)
  }
}
