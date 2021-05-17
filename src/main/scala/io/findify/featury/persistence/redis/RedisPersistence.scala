package io.findify.featury.persistence.redis

import cats.effect.IO
import cats.effect.kernel.Resource
import io.findify.featury.feature.{BoundedList, Counter, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.model.FeatureValue
import io.findify.featury.persistence.{Persistence, ValueStore}
import redis.clients.jedis.Jedis

class RedisPersistence(val redis: Jedis) extends Persistence {
  override def periodicCounter(config: PeriodicCounter.PeriodicCounterConfig): IO[PeriodicCounter] =
    IO.pure(new RedisPeriodicCounter(config, redis))

  override def numBoundedList(config: BoundedList.BoundedListConfig): IO[BoundedList[FeatureValue.Num]] =
    IO.pure(new RedisBoundedList.RedisNumBoundedList(redis, config))

  override def textBoundedList(config: BoundedList.BoundedListConfig): IO[BoundedList[FeatureValue.Text]] =
    IO.pure(new RedisBoundedList.RedisTextBoundedList(redis, config))

  override def statsEstimator(config: StatsEstimator.StatsEstimatorConfig): IO[StatsEstimator] =
    IO.pure(new RedisStatsEstimator(config, redis))

  override def counter(config: Counter.CounterConfig): IO[Counter] =
    IO.pure(new RedisCounter(config, redis))

  override def freqEstimator(config: FreqEstimator.FreqEstimatorConfig): IO[FreqEstimator] =
    IO.pure(new RedisFreqEstimator(config, redis))

  override def values(): IO[ValueStore] =
    IO.pure(new RedisValues(redis))
}

object RedisPersistence {
  def resource(host: String, port: Int = 6379) =
    Resource.make(IO { new RedisPersistence(new Jedis(host, port)) })(redis => IO(redis.redis.close()))
}
