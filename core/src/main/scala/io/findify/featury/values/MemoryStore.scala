package io.findify.featury.values

import cats.effect.IO
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{FeatureValue, Key}

import scala.collection.mutable

case class MemoryStore() extends FeatureStore {
  val cache = mutable.Map[Key, FeatureValue]()

  override def write(batch: List[FeatureValue]): IO[Unit] =
    IO(batch.foreach(v => cache.put(v.key, v)))

  override def read(request: ReadRequest): IO[ReadResponse] = IO {
    val values = for {
      key   <- request.keys
      value <- cache.get(key)
    } yield {
      value
    }
    ReadResponse(values)
  }

  override def close(): IO[Unit] = IO.unit
}
