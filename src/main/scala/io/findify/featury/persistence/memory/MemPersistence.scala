package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import io.findify.featury.feature.BoundedList.BoundedListState
import io.findify.featury.feature.Counter.CounterState
import io.findify.featury.feature.{BoundedList, Counter}
import io.findify.featury.model.FeatureValue.{Num, Text}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.persistence.Persistence
import io.findify.featury.persistence.memory.MemBoundedList.{MemNumBoundedList, MemTextBoundedList}

case object MemPersistence extends Persistence {
  lazy val counters   = Scaffeine().build[Key, CounterState]()
  lazy val textLists  = Scaffeine().build[Key, BoundedListState[Text]]()
  lazy val numLists   = Scaffeine().build[Key, BoundedListState[Num]]()
  lazy val valueCache = Scaffeine().build[Key, Map[FeatureName, FeatureValue]]()

  override def counter(config: Counter.CounterConfig): Counter = new MemCounter(config, counters)

  override def textBoundedList(
      config: BoundedList.BoundedListConfig
  ): BoundedList[FeatureValue.Text] =
    new MemTextBoundedList(config, textLists)

  override def numBoundedList(config: BoundedList.BoundedListConfig): BoundedList[FeatureValue.Num] =
    new MemNumBoundedList(config, numLists)

  override def values() = new MemValues(valueCache)
}
