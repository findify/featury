package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.ItemFeatures
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.persistence.ValueStore

class MemValues(values: Cache[Key, FeatureValue]) extends ValueStore {
  override def readBatch(keys: List[Key]): IO[List[ItemFeatures]] = IO {
    keys
      .map(key =>
        values.getIfPresent(key) match {
          case Some(value) => ItemFeatures(key, Some(value))
          case None        => ItemFeatures(key, None)
        }
      )
  }

  override def write(key: Key, value: FeatureValue): IO[Unit] = IO {
    values.put(key, value)
  }
}
