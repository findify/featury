package io.findify.featury.persistence.memory

import io.findify.featury.feature.BoundedList.BoundedListState
import io.findify.featury.feature.Counter.CounterState
import io.findify.featury.model.Feature.State
import io.findify.featury.model.FeatureValue.{Num, NumValue, Text}
import io.findify.featury.model.{FeatureValue, Key, StoreConfig}
import io.findify.featury.persistence.StateStore.StateDelta
import io.findify.featury.persistence.memory.MemoryStore.{
  CounterMemoryStore,
  MemoryCache,
  NumListMemoryStore,
  TextListMemoryStore
}
import io.findify.featury.persistence.{StateStore, Store}
import cats.effect.IO
import com.github.blemale.scaffeine.Scaffeine

case class MemoryStore(conf: StoreConfig) extends Store {
  override lazy val counter  = CounterMemoryStore(conf)
  override lazy val textList = TextListMemoryStore(conf)
  override lazy val numList  = NumListMemoryStore(conf)

}

object MemoryStore {
  case class CounterMemoryStore(conf: StoreConfig)  extends MemoryCache[CounterState]
  case class TextListMemoryStore(conf: StoreConfig) extends MemoryCache[BoundedListState[Text]]
  case class NumListMemoryStore(conf: StoreConfig)  extends MemoryCache[BoundedListState[Num]]

  trait MemoryCache[S <: State] extends StateStore[S] {
    def conf: StoreConfig
    lazy val cache = Scaffeine().expireAfterAccess(conf.ttl).build[Key, S]()

    override def read(keys: List[Key]): IO[Map[Key, S]] = IO {
      val result = for {
        key   <- keys
        state <- cache.getIfPresent(key)
      } yield {
        key -> state
      }
      result.toMap
    }
    override def write(keys: List[(Key, StateStore.StateDelta[S])]): IO[Unit] = IO {
      keys.foreach { case (key, StateDelta(_, after)) => cache.put(key, after) }
    }
  }
}
