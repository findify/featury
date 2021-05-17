package io.findify.featury.service

import cats.effect.IO
import io.findify.featury.model.{BackendError, Key, Schema, WriteRequest}
import io.findify.featury.model.WriteRequest.WriteAction
import io.findify.featury.persistence.{Persistence, ValueStore}
import cats.implicits._
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.{BoundedList, Counter, Feature, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.feature.FreqEstimator.FreqEstimatorConfig
import io.findify.featury.feature.PeriodicCounter.PeriodicCounterConfig
import io.findify.featury.feature.StatsEstimator.StatsEstimatorConfig
import io.findify.featury.model.FeatureValue.{Num, NumType, Text, TextType}
import io.findify.featury.model.Key.FeatureKey
import io.findify.featury.model.Schema.FeatureConfig
import io.findify.featury.persistence.ValueStore.{BatchResult, KeyBatch}

class Service(
    counters: Map[FeatureKey, Counter],
    periodicCounters: Map[FeatureKey, PeriodicCounter],
    textLists: Map[FeatureKey, BoundedList[Text]],
    numLists: Map[FeatureKey, BoundedList[Num]],
    freqs: Map[FeatureKey, FreqEstimator],
    stats: Map[FeatureKey, StatsEstimator],
    values: ValueStore
) {

  def write(action: WriteAction) = action match {
    case WriteRequest.Increment(key, inc) =>
      dispatch[Counter](key, counters, _.increment(key, inc))

    case WriteRequest.PeriodicIncrement(key, ts, inc) =>
      dispatch[PeriodicCounter](key, periodicCounters, _.increment(key, ts, inc))

    case WriteRequest.AppendText(key, ts, value) =>
      dispatch[BoundedList[Text]](key, textLists, _.put(key, Text(value), ts))

    case WriteRequest.AppendNum(key, ts, value) =>
      dispatch[BoundedList[Num]](key, numLists, _.put(key, Num(value), ts))

    case WriteRequest.PutStatSample(key, value) =>
      dispatch[StatsEstimator](key, stats, _.put(key, value))

    case WriteRequest.PutFreqSample(key, value) =>
      dispatch[FreqEstimator](key, freqs, _.put(key, value))
  }

  private def dispatch[F <: Feature[_, _, _]](key: Key, mapping: Map[FeatureKey, F], f: F => IO[Unit]): IO[Unit] =
    mapping.get(key.featureKey) match {
      case Some(c) => f(c)
      case None    => IO.raiseError(BackendError(s"feature $key not found"))
    }

  def read(keys: KeyBatch): IO[BatchResult] = {
    values.readBatch(keys)
  }

}

object Service {
  def load(schema: Schema, store: Persistence): IO[Service] = for {
    counters <- collect(schema) { case c: CounterConfig => store.counter(c) }
    periodic <- collect(schema) { case c: PeriodicCounterConfig => store.periodicCounter(c) }
    textList <- collect(schema) { case c @ BoundedListConfig(_, _, _, _, _, TextType) => store.textBoundedList(c) }
    numList  <- collect(schema) { case c @ BoundedListConfig(_, _, _, _, _, NumType) => store.numBoundedList(c) }
    freqs    <- collect(schema) { case c: FreqEstimatorConfig => store.freqEstimator(c) }
    stats    <- collect(schema) { case c: StatsEstimatorConfig => store.statsEstimator(c) }
    values   <- store.values()
  } yield {
    new Service(counters, periodic, textList, numList, freqs, stats, values)
  }

  def collect[F <: Feature[_, _, _]](
      schema: Schema
  )(pf: PartialFunction[FeatureConfig, IO[F]]): IO[Map[FeatureKey, F]] = {
    val result = for {
      ns          <- schema.namespaces
      group       <- ns.groups
      featureConf <- group.features
      feature     <- pf.lift(featureConf)
    } yield {
      feature.map(f => FeatureKey(ns.name, group.name, featureConf.name) -> f)
    }
    result.sequence.map(_.toMap)
  }
}
