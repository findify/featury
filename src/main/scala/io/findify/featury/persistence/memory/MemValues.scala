package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.ReadResponse.ItemFeatures
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.persistence.ValueStore
import io.findify.featury.persistence.ValueStore.{BatchResult, KeyBatch, KeyFeatures}

class MemValues(values: Cache[Key, FeatureValue]) extends ValueStore {
  override def readBatch(key: KeyBatch): IO[BatchResult] = IO {
    BatchResult(
      ns = key.ns,
      group = key.group,
      values = key.ids
        .map(id =>
          KeyFeatures(
            id = id,
            features = key.featureNames
              .flatMap(f => values.getIfPresent(key.asKey(f, id)).map(value => f -> value))
              .toMap
          )
        )
        .filter(_.features.nonEmpty)
    )
  }

  override def write(key: Key, value: FeatureValue): IO[Unit] = IO {
    values.put(key, value)
  }
}
