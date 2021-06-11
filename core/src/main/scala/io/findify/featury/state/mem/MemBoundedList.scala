package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model._
import io.findify.featury.model.Write._

case class MemBoundedList(config: BoundedListConfig, cache: Cache[Key, List[TimeValue]]) extends BoundedList {
  override def put(action: Append): Unit = cache.getIfPresent(action.key) match {
    case None => cache.put(action.key, List(TimeValue(action.ts, action.value)))
    case Some(cached) =>
      val result   = TimeValue(action.ts, action.value) :: cached
      val filtered = result.filter(_.ts.isAfterOrEquals(action.ts.minus(config.duration))).take(config.count)
      cache.put(action.key, filtered)
  }

  override def computeValue(key: Key, ts: Timestamp): Option[BoundedListValue] =
    cache.getIfPresent(key).map(BoundedListValue(key, ts, _))

  override def readState(key: Key, ts: Timestamp): Option[BoundedListState] =
    cache.getIfPresent(key).map(items => BoundedListState(key, ts, items))

  override def writeState(state: BoundedListState): Unit = cache.put(state.key, state.values)
}
