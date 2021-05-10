package io.findify.featury.service

import cats.effect.IO
import io.findify.featury.model.{Key, Schema}
import io.findify.featury.model.WriteRequest.WriteAction
import io.findify.featury.persistence.Persistence
import io.findify.featury.service.FeatureService.{CounterService, NumBoundedListService, TextBoundedListService}
import cats.implicits._
import io.findify.featury.model.ReadResponse.ItemFeatures

class Service(schema: Schema, store: Persistence) {
  val counters  = CounterService(schema, store)
  val textLists = TextBoundedListService(schema, store)
  val numLists  = NumBoundedListService(schema, store)
  val values    = store.values()

  def write(actions: List[WriteAction]): IO[Unit] = {
    val result = List(
      counters.write(actions),
      textLists.write(actions),
      numLists.write(actions)
    )
    result.sequence.map(_ => {})
  }

  def read(keys: List[Key]): IO[List[ItemFeatures]] = {
    values.readBatch(keys)
  }
}
