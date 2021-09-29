package io.findify.featury.connector.redis

import cats.effect.{IO, Resource}
import io.findify.featury.connector.redis.RedisStore.RedisKey
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{FeatureKey, FeatureValue, Key}
import io.findify.featury.values.ValueStoreConfig.RedisConfig
import io.findify.featury.values.{FeatureStore, StoreCodec}
import redis.clients.jedis.Jedis

import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds
import scala.concurrent.duration._

case class RedisStore(config: RedisConfig, expires: Map[FeatureKey, FiniteDuration] = Map.empty) extends FeatureStore {

  @transient lazy val client: Jedis = new Jedis(config.host, config.port)
  val DEFAULT_EXPIRE                = 60.days

  override def write(batch: List[FeatureValue]): Unit = {
    val values = batch.flatMap(fv => List(RedisKey(fv.key).bytes, config.codec.encode(fv)))
    client.mset(values: _*)
    val tx = client.multi()
    for {
      fv <- batch
      expire = expires.getOrElse(FeatureKey(fv.key), DEFAULT_EXPIRE)
      key    = RedisKey(fv.key).bytes
    } {
      tx.expire(key, expire.toSeconds)
    }
    tx.exec()
  }

  override def read(request: ReadRequest): IO[ReadResponse] = {
    val keys = request.keys.map(RedisKey.apply)
    for {
      response <- IO(client.mget(keys.map(_.bytes): _*))
      decoded  <- decodeResponse(response.asScala.toList)
    } yield {
      ReadResponse(decoded)
    }
  }

  private def decodeResponse(responses: List[Array[Byte]], acc: List[FeatureValue] = Nil): IO[List[FeatureValue]] =
    responses match {
      case Nil          => IO.pure(acc)
      case null :: tail => decodeResponse(tail, acc)
      case head :: tail =>
        config.codec.decode(head) match {
          case Left(err)    => IO.raiseError(err)
          case Right(value) => decodeResponse(tail, value :: acc)
        }
    }

  override def close(): Unit = { client.close() }
}

object RedisStore {

  case class RedisKey(key: Key) {
    val bytes =
      s"${key.tag.scope.name}/${key.tenant.value}/${key.name.value}/${key.tag.value}".getBytes(StandardCharsets.UTF_8)
  }
  object RedisKey {
    def apply(bytes: Array[Byte]): RedisKey = {
      val tokens = new String(bytes, StandardCharsets.UTF_8).split('/')
      new RedisKey(Key(tag = Tag(Scope(tokens(0)), tokens(3)), name = FeatureName(tokens(2)), Tenant(tokens(1))))
    }
  }

}
