package io.findify.featury.service

import io.findify.featury.feature.Counter.{CounterConfig, CounterFeature, Increment}
import io.findify.featury.model.Feature.{Op, State}
import io.findify.featury.model.Schema.{CounterFeatureSchema, FeatureConfig, FeatureSchema}
import io.findify.featury.model.WriteRequest.WriteAction
import io.findify.featury.model.{
  Feature,
  FeatureName,
  FeatureValue,
  Key,
  ReadRequest,
  ReadResponse,
  Schema,
  WriteRequest
}
import io.findify.featury.persistence.StateStore.StateDelta
import io.findify.featury.persistence.{StateStore, Store}
import cats.effect.IO

import scala.concurrent.duration._

class FeatureService(schema: Schema, store: Store) {
  def write(request: WriteRequest): IO[Unit] = {
    for {
      counterUpdates <- writeOne(
        actions = request.actions,
        select = selectCounters,
        state = store.counter,
        feature = CounterFeature,
        loadConf = schema.counterConfigs.get
      )
    } yield {}
  }

  def writeOne[O <: Op, S <: State, T <: FeatureValue, C <: FeatureConfig](
      actions: List[WriteAction],
      select: WriteAction => Option[(Key, O)],
      state: StateStore[S],
      feature: Feature[S, T, O, C],
      loadConf: FeatureName => Option[C]
  ) = {
    val grouped = actions
      .flatMap(select(_))
      .groupBy(_._1)
      .map { case (key, list) =>
        key -> list.map(_._2)
      }
    val keys = grouped.keys.toList
    for {
      currentState <- state.read(keys)
      updates <- IO {
        for {
          key  <- keys
          conf <- loadConf(FeatureName(key.group, key.name))
          ops  <- grouped.get(key)
          before  = currentState.getOrElse(key, feature.emptyState(conf))
          updated = ops.foldLeft(before)((state, inc) => feature.update(conf, state, inc))
          if before != updated
        } yield {
          key -> StateDelta(before, updated)
        }
      }
      _ <- state.write(updates)
    } yield {
      updates
    }
  }

  def selectCounters(action: WriteAction) = action.op match {
    case inc @ Increment(_, _) => Some(Key(action.group, action.feature, action.tenant, action.key) -> inc)
    case _                     => None
  }

  def read(request: ReadRequest): IO[ReadResponse] = { ??? }
}
