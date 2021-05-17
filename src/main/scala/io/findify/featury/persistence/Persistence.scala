package io.findify.featury.persistence

import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, Counter, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.FreqEstimator.{FreqEstimatorConfig, FreqEstimatorState}
import io.findify.featury.feature.PeriodicCounter.PeriodicCounterConfig
import io.findify.featury.feature.StatsEstimator.StatsEstimatorConfig
import io.findify.featury.model.FeatureValue.{Num, Text}

trait Persistence {
  def values(): ValueStore
  def counter(config: CounterConfig): Counter
  def textBoundedList(config: BoundedListConfig): BoundedList[Text]
  def numBoundedList(config: BoundedListConfig): BoundedList[Num]
  def periodicCounter(config: PeriodicCounterConfig): PeriodicCounter
  def statsEstimator(config: StatsEstimatorConfig): StatsEstimator
  def freqEstimator(config: FreqEstimatorConfig): FreqEstimator
}
