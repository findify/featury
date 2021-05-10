package io.findify.featury.persistence

import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, Counter}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.model.FeatureValue.{Num, Text}

trait Persistence {
  def values(): ValueStore
  def counter(config: CounterConfig): Counter
  def textBoundedList(config: BoundedListConfig): BoundedList[Text]
  def numBoundedList(config: BoundedListConfig): BoundedList[Num]
}
