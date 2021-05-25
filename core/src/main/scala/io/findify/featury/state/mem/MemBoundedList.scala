package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model._
import io.findify.featury.model.Write._

sealed trait MemBoundedList[T <: Scalar, I <: ListItem[T]] extends BoundedList[T] {
  def cache: Cache[Key, List[I]]
  def makeItem(action: Append[T]): I
  def makeValue(key: Key, ts: Timestamp, items: List[I]): BoundedListValue[T]
  override def put(action: Append[T]): Unit = cache.getIfPresent(action.key) match {
    case None => cache.put(action.key, List(makeItem(action)))
    case Some(cached) =>
      val result   = makeItem(action) :: cached
      val filtered = result.filter(_.ts.isAfterOrEquals(action.ts.minus(config.duration))).take(config.count)
      cache.put(action.key, filtered)
  }

  override def computeValue(key: Key, ts: Timestamp): Option[BoundedListValue[T]] =
    cache.getIfPresent(key).map(makeValue(key, ts, _))
}

object MemBoundedList {
  case class MemTextBoundedList(config: BoundedListConfig, cache: Cache[Key, List[StringListItem]])
      extends MemBoundedList[SString, StringListItem] {
    override def makeItem(action: Append[SString]): StringListItem = StringListItem(action.value, action.ts)
    override def makeValue(key: Key, ts: Timestamp, items: List[StringListItem]): BoundedListValue[SString] =
      StringBoundedListValue(key, ts, items)
  }

  case class MemNumBoundedList(config: BoundedListConfig, cache: Cache[Key, List[DoubleListItem]])
      extends MemBoundedList[SDouble, DoubleListItem] {
    override def makeItem(action: Append[SDouble]): DoubleListItem = DoubleListItem(action.value, action.ts)
    override def makeValue(key: Key, ts: Timestamp, items: List[DoubleListItem]): BoundedListValue[SDouble] =
      DoubleBoundedListValue(key, ts, items)
  }
}
