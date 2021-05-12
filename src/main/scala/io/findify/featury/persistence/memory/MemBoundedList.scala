package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import io.findify.featury.feature.BoundedList
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.model.FeatureValue.{ListItem, Num, NumBoundedListValue, Scalar, Text, TextBoundedListValue}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.{FeatureValue, Key, Timestamp}

trait MemBoundedList[T <: Scalar] extends BoundedList[T] {
  def listCache: Cache[Key, BoundedListState[T]] = Scaffeine().build()

  override def readState(key: Key): IO[BoundedListState[T]] = IO {
    listCache.getIfPresent(key).getOrElse(empty())
  }

  override def put(key: Key, value: T, ts: Timestamp): IO[Unit] = IO {
    listCache.getIfPresent(key) match {
      case Some(existing) => listCache.put(key, BoundedListState(ListItem(value, ts) :: existing.values))
      case None           => listCache.put(key, BoundedListState(List(ListItem(value, ts))))
    }
  }
}

object MemBoundedList {
  class MemTextBoundedList(
      val config: BoundedListConfig,
      override val listCache: Cache[Key, BoundedListState[Text]]
  ) extends MemBoundedList[Text] {
    override def fromItems(list: List[ListItem[Text]]): FeatureValue.BoundedListValue[Text] = TextBoundedListValue(list)
  }

  class MemNumBoundedList(val config: BoundedListConfig, override val listCache: Cache[Key, BoundedListState[Num]])
      extends MemBoundedList[Num] {
    override def fromItems(list: List[ListItem[Num]]): FeatureValue.BoundedListValue[Num] = NumBoundedListValue(list)
  }
}
