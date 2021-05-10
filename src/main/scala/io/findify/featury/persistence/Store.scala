package io.findify.featury.persistence

import io.findify.featury.feature.BoundedList.BoundedListState
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.model.FeatureValue.{Num, NumValue, Text}

trait Store {
  def counter: StateStore[CounterState]
  def textList: StateStore[BoundedListState[Text]]
  def numList: StateStore[BoundedListState[Num]]
}
