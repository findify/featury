package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.{FeatureNameValue, ItemFeatures}
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.persistence.ValueStore

class MemValues(values: Cache[Key, Map[FeatureName, FeatureValue]]) extends ValueStore {
  override def readBatch(keys: List[Key]): IO[List[ItemFeatures]] = IO {
    keys
      .map(key =>
        values.getIfPresent(key) match {
          case Some(value) => ItemFeatures(key, value.map(kv => FeatureNameValue(kv._1, kv._2)).toList)
          case None        => ItemFeatures(key, Nil)
        }
      )
  }

  override def write(key: Key, name: FeatureName, value: FeatureValue): IO[Unit] = IO {
    values.getIfPresent(key) match {
      case Some(existing) => values.put(key, existing + (name -> value))
      case None           => values.put(key, Map(name -> value))
    }
  }
}
