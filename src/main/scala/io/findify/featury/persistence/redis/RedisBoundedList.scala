package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.BoundedList
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.model.FeatureValue.{ListItem, Num, NumBoundedListValue, Scalar, Text, TextBoundedListValue}
import io.findify.featury.model.{FeatureValue, Key, Timestamp}
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.collection.JavaConverters._

trait RedisBoundedList[T <: Scalar] extends BoundedList[T] {
  import KeyCodec._

  def redis: Jedis
  def codec: Codec[ListItem[T]]

  override def put(key: Key, value: T, ts: Timestamp): IO[Unit] = IO {
    redis.rpush(key.toRedisKey("state"), codec.encode(ListItem(value, ts)))
  }

  override def readState(key: Key): IO[BoundedList.BoundedListState[T]] = for {
    list    <- IO { redis.lrange(key.toRedisKey("state"), 0, -1).asScala.toList }
    decoded <- decodeList(list)
  } yield {
    decoded
  }

  @tailrec
  private def decodeList(items: List[String], acc: List[ListItem[T]] = Nil): IO[BoundedListState[T]] = items match {
    case Nil => IO.pure(BoundedListState(acc))
    case head :: tail =>
      codec.decode(head) match {
        case Left(err)    => IO.raiseError(err)
        case Right(value) => decodeList(tail, value :: acc)
      }
  }
}

object RedisBoundedList {
  class RedisTextBoundedList(val redis: Jedis, val config: BoundedListConfig)(implicit val codec: Codec[ListItem[Text]])
      extends RedisBoundedList[Text] {
    override def fromItems(list: List[ListItem[Text]]): FeatureValue.BoundedListValue[Text] = TextBoundedListValue(list)
  }
  class RedisNumBoundedList(val redis: Jedis, val config: BoundedListConfig)(implicit val codec: Codec[ListItem[Num]])
      extends RedisBoundedList[Num] {
    override def fromItems(list: List[ListItem[Num]]): FeatureValue.BoundedListValue[Num] = NumBoundedListValue(list)
  }
}
