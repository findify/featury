package io.findify.featury.persistence.redis

import cats.effect.IO
import cats.effect.kernel.Resource
import io.findify.featury.feature.{BoundedList, Counter, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.model.FeatureValue
import io.findify.featury.persistence.{Persistence, ValueStore}
import redis.clients.jedis.Jedis

class RedisPersistence(val redis: Jedis) extends Persistence {
  override def periodicCounter(config: PeriodicCounter.PeriodicCounterConfig): PeriodicCounter =
    ???

  override def numBoundedList(config: BoundedList.BoundedListConfig): BoundedList[FeatureValue.Num] =
    new RedisBoundedList.RedisNumBoundedList(redis, config)

  override def textBoundedList(config: BoundedList.BoundedListConfig): BoundedList[FeatureValue.Text] =
    new RedisBoundedList.RedisTextBoundedList(redis, config)

  override def statsEstimator(config: StatsEstimator.StatsEstimatorConfig): StatsEstimator =
    new RedisStatsEstimator(config, redis)

  override def counter(config: Counter.CounterConfig): Counter =
    new RedisCounter(config, redis)

  override def freqEstimator(config: FreqEstimator.FreqEstimatorConfig): FreqEstimator =
    new RedisFreqEstimator(config, redis)

  override def values(): ValueStore =
    new RedisValues(redis)
}

object RedisPersistence {
  def resource(host: String, port: Int = 6379) =
    Resource.make(IO { new RedisPersistence(new Jedis(host, port)) })(redis => IO(redis.redis.close()))
}
