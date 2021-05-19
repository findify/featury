package io.findify.featury.persistence.redis

import io.findify.featury.model.FeatureValue.ListItem
import redis.clients.jedis.Jedis

trait RedisFeature {
  def keySuffix: String
  def redis: Jedis
}
