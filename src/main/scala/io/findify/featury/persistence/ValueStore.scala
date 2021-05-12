package io.findify.featury.persistence

import io.findify.featury.model.{FeatureValue, Key}
import cats.effect.IO
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.ItemFeatures

trait ValueStore {
  def write(key: Key, value: FeatureValue): IO[Unit]
  def readBatch(keys: List[Key]): IO[List[ItemFeatures]]
}
