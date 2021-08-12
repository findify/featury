package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.MapFeature
import io.findify.featury.model.FeatureConfig.{MapConfig, ScalarConfig}
import io.findify.featury.model.{Key, MapState, MapValue, Scalar, Timestamp, Write}

case class MemMapFeature(config: MapConfig, cache: Cache[Key, Map[String, Scalar]]) extends MapFeature {
  override def put(action: Write.PutTuple): Unit = {
    val map = cache
      .getIfPresent(action.key)
      .getOrElse(Map.empty)
    action.value match {
      case Some(value) =>
        cache.put(action.key, map + (action.mapKey -> value))
      case None =>
        cache.put(action.key, map - action.mapKey)
    }

  }

  override def writeState(state: MapState): Unit = {
    cache.put(state.key, state.values)
  }

  override def readState(key: Key, ts: Timestamp): Option[MapState] =
    cache.getIfPresent(key).filter(_.nonEmpty).map(s => MapState(key, ts, s))

  override def computeValue(key: Key, ts: Timestamp): Option[MapValue] =
    cache.getIfPresent(key).filter(_.nonEmpty).map(s => MapValue(key, ts, s))
}
