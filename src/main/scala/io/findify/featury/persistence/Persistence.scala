package io.findify.featury.persistence

import cats.effect.IO
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, Counter, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.FreqEstimator.{FreqEstimatorConfig, FreqEstimatorState}
import io.findify.featury.feature.PeriodicCounter.PeriodicCounterConfig
import io.findify.featury.feature.StatsEstimator.StatsEstimatorConfig
import io.findify.featury.model.FeatureValue.{Num, Text}

trait Persistence {
  def values(): IO[ValueStore]
  def counter(config: CounterConfig): IO[Counter]
  def textBoundedList(config: BoundedListConfig): IO[BoundedList[Text]]
  def numBoundedList(config: BoundedListConfig): IO[BoundedList[Num]]
  def periodicCounter(config: PeriodicCounterConfig): IO[PeriodicCounter]
  def statsEstimator(config: StatsEstimatorConfig): IO[StatsEstimator]
  def freqEstimator(config: FreqEstimatorConfig): IO[FreqEstimator]
}
