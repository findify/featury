package io.findify.featury.persistence.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model.FeatureValue.{BoundedListValue, ListItem, Num, Scalar, Text}
import io.findify.featury.model.{FeatureValue, Key, WriteRequest}

sealed trait MemBoundedList[T <: Scalar] extends BoundedList[T] {
  def cache: Cache[Key, BoundedListValue[T]]
  override def put(action: WriteRequest.Append[T]): Unit = cache.getIfPresent(action.key) match {
    case None => cache.put(action.key, BoundedListValue(List(ListItem(action.value, action.ts))))
    case Some(cached) =>
      val result   = ListItem(action.value, action.ts) :: cached.value
      val filtered = result.filter(_.ts.isAfterOrEquals(action.ts.minus(config.duration))).take(config.count)
      cache.put(action.key, BoundedListValue(filtered))
  }

  override def computeValue(key: Key): Option[FeatureValue.BoundedListValue[T]] =
    cache.getIfPresent(key)
}

object MemBoundedList {
  case class MemTextBoundedList(config: BoundedListConfig, cache: Cache[Key, BoundedListValue[Text]])
      extends MemBoundedList[Text]
  case class MemNumBoundedList(config: BoundedListConfig, cache: Cache[Key, BoundedListValue[Num]])
      extends MemBoundedList[Num]
}
