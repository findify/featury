package io.findify.featury.connector.redis

import cats.effect.{IO, OutcomeIO, Poll, Resource}
import io.findify.featury.connector.redis.RedisStore.RedisKey
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{FeatureKey, FeatureValue, Key}
import io.findify.featury.values.ValueStoreConfig.RedisConfig
import io.findify.featury.values.{FeatureStore, StoreCodec}
import redis.clients.jedis.{Jedis, JedisPool}

import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

case class RedisStore(config: RedisConfig, expires: Map[FeatureKey, FiniteDuration] = Map.empty) extends FeatureStore {
  @transient lazy val clientPool = new JedisPool(config.host, config.port)
  val DEFAULT_EXPIRE             = 60.days

  override def write(batch: List[FeatureValue]): IO[Unit] = {
    IO.bracketFull((poll: Poll[IO]) => IO { clientPool.getResource })((client: Jedis) =>
      IO {

        val values = batch.flatMap(fv => List(RedisKey(fv.key).bytes, config.codec.encode(fv)))
        val tx     = client.pipelined()
        for {
          fv <- batch
          expire = expires.getOrElse(FeatureKey(fv.key), DEFAULT_EXPIRE)
          key    = RedisKey(fv.key).bytes
        } {
          tx.expire(key, expire.toSeconds)
        }
        tx.mset(values: _*)
        tx.sync()
      }
    )((client: Jedis, _: OutcomeIO[Unit]) => IO { clientPool.returnResource(client) })

  }

  override def read(request: ReadRequest): IO[ReadResponse] = {
    val keys = request.keys.map(RedisKey.apply)
    IO.bracketFull(_ => IO { clientPool.getResource })((client: Jedis) =>
      for {
        response <- IO(client.mget(keys.map(_.bytes): _*))
        decoded  <- decodeResponse(response.asScala.toList)
      } yield {
        ReadResponse(decoded)
      }
    )((client: Jedis, _) => IO { clientPool.returnResource(client) })
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

  override def close(): IO[Unit] = IO { clientPool.close() }

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
