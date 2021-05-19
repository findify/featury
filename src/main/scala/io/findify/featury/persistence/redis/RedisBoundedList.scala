package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.BoundedList
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.model.FeatureValue.{ListItem, Num, NumBoundedListValue, Scalar, Text, TextBoundedListValue}
import io.findify.featury.model.{FeatureValue, Key, Timestamp}
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.collection.JavaConverters._

trait RedisBoundedList[T <: Scalar] extends BoundedList[T] with RedisFeature {
  import KeyCodec._

  override val keySuffix: String = "bl"

  def codec: Codec[ListItem[T]]

  override def put(key: Key, value: T, ts: Timestamp): IO[Unit] = IO {
    redis.rpush(key.toRedisKey(keySuffix), codec.encode(ListItem(value, ts)))
  }

  override def readState(key: Key): IO[Option[BoundedListState[T]]] = for {
    list    <- IO { redis.lrange(key.toRedisKey(keySuffix), 0, -1).asScala.toList }
    decoded <- decodeList(list)
  } yield {
    if (decoded.values.nonEmpty) Some(decoded) else None
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
  case class RedisTextBoundedList(redis: Jedis, config: BoundedListConfig)(implicit val codec: Codec[ListItem[Text]])
      extends RedisBoundedList[Text] {
    override def fromItems(list: List[ListItem[Text]]): FeatureValue.BoundedListValue[Text] = TextBoundedListValue(list)
  }
  case class RedisNumBoundedList(redis: Jedis, config: BoundedListConfig)(implicit val codec: Codec[ListItem[Num]])
      extends RedisBoundedList[Num] {
    override def fromItems(list: List[ListItem[Num]]): FeatureValue.BoundedListValue[Num] = NumBoundedListValue(list)
  }
}
