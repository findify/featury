package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.BoundedList
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.model.FeatureValue.{ListItem, Num, Scalar, Text}
import io.findify.featury.model.{Key, Timestamp}
import io.findify.featury.persistence.codec.Codec
import io.findify.featury.persistence.codec.Codec.DecodingError
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait RedisBoundedList[T <: Scalar] extends BoundedList[T] {
  import KeyCodec._

  def redis: Jedis
  def codec: Codec[T]

  override def put(key: Key, value: T, ts: Timestamp): IO[Unit] = IO {
    redis.rpush(key.toRedisKey, encode(value, ts))
  }

  override def readState(key: Key): IO[BoundedList.BoundedListState[T]] = for {
    list    <- IO { redis.lrange(key.toRedisKey, 0, -1).asScala.toList }
    decoded <- decodeList(list)
  } yield {
    decoded
  }

  def encode(value: T, ts: Timestamp): String = s"${ts.ts}:${codec.encode(value)}"
  def decode(in: String): Either[DecodingError, ListItem[T]] = {
    val firstSeparator = in.indexOf(":")
    if (firstSeparator <= 0) {
      Left(DecodingError(s"cannot decode list item $in"))
    } else {
      val ts    = in.substring(0, firstSeparator)
      val value = in.substring(firstSeparator + 1, in.length)
      Try(java.lang.Long.parseLong(ts)) match {
        case Failure(exception) => Left(DecodingError(s"wrong timestamp: ${ts} due to $exception"))
        case Success(ts) =>
          codec.decode(value) match {
            case Left(err)    => Left(DecodingError(s"cannot decode value $value: $err"))
            case Right(value) => Right(ListItem(value, Timestamp(ts)))
          }
      }
    }
  }

  @tailrec
  private def decodeList(items: List[String], acc: List[ListItem[T]] = Nil): IO[BoundedListState[T]] = items match {
    case Nil => IO.pure(BoundedListState(acc))
    case head :: tail =>
      decode(head) match {
        case Left(err)    => IO.raiseError(err)
        case Right(value) => decodeList(tail, value :: acc)
      }
  }
}

object RedisBoundedList {
  class RedisTextBoundedList(val redis: Jedis, val config: BoundedListConfig) extends RedisBoundedList[Text] {
    override lazy val codec = Codec.textCodec
  }
  class RedisNumBoundedList(val redis: Jedis, val config: BoundedListConfig) extends RedisBoundedList[Num] {
    override lazy val codec = Codec.numCodec
  }
}
