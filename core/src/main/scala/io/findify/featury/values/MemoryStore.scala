package io.findify.featury.values

import cats.effect.IO
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{FeatureValue, Key}

import scala.collection.mutable

class MemoryStore extends FeatureStore {
  val cache = mutable.Map[Key, FeatureValue]()

  override def write(batch: List[FeatureValue]): Unit =
    batch.foreach(v => cache.put(v.key, v))

  override def read(request: ReadRequest): IO[ReadResponse] = IO {
    val values = for {
      id    <- request.ids
      name  <- request.features
      value <- cache.get(Key(request.ns, request.scope, name, request.tenant, id))
    } yield {
      value
    }
    ReadResponse(values)
  }
}
