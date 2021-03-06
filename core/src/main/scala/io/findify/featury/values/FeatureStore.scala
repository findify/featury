package io.findify.featury.values

import cats.effect.IO
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.api.{ReadRequest, ReadResponse}

trait FeatureStore {
  def write(batch: List[FeatureValue]): IO[Unit]
  def writeSync(batch: List[FeatureValue]): Unit
  def read(request: ReadRequest): IO[ReadResponse]
  def close(): IO[Unit]
  def closeSync(): Unit
}
