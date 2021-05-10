package io.findify.featury.persistence

import io.findify.featury.model.{FeatureValue, Key}
import cats.effect.IO
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.{FeatureNameValue, ItemFeatures}

trait ValueStore {
  def write(key: Key, name: FeatureName, value: FeatureValue): IO[Unit]
  def readBatch(keys: List[Key]): IO[List[ItemFeatures]]
}
