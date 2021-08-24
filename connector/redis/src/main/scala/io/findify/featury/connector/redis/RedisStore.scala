package io.findify.featury.connector.redis

import cats.effect.{IO, Resource}
import io.findify.featury.connector.redis.RedisStore.RedisKey
import io.findify.featury.model.Key.{Namespace, Scope, Tag, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.values.StoreCodec.DecodingError
import io.findify.featury.values.ValueStoreConfig.RedisConfig
import io.findify.featury.values.{FeatureStore, StoreCodec}
import redis.clients.jedis.Jedis

import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._
import scala.language.higherKinds

case class RedisStore(client: Jedis, codec: StoreCodec) extends FeatureStore {

  override def write(batch: List[FeatureValue]): Unit = {
    val transaction = client.multi()
    batch.foreach(fv => transaction.hset(RedisKey(fv.key).bytes, fv.key.name.value.getBytes, codec.encode(fv)))
    transaction.exec()
  }

  override def read(request: ReadRequest): IO[ReadResponse] = {
    val transaction = client.multi()
    for {
      tag <- request.tags
    } {
      transaction.hmget(
        RedisKey(request.ns, request.tenant, tag).bytes,
        request.features.map(_.value.getBytes(StandardCharsets.UTF_8)): _*
      )
    }
    for {
      response <- IO(transaction.exec())
      decoded  <- decodeResponses(response.asScala.toList)
    } yield {
      ReadResponse(decoded)
    }
  }

  private def decodeResponses(responses: List[AnyRef], acc: List[FeatureValue] = Nil): IO[List[FeatureValue]] =
    responses match {
      case Nil => IO.pure(acc)
      case head :: tail =>
        head match {
          case bytes: java.util.List[Array[Byte]] =>
            decodeResponse(bytes.asScala.toList).flatMap(values => decodeResponses(tail, values ++ acc))
          case _ => IO.raiseError(DecodingError(""))
        }
    }

  private def decodeResponse(response: List[Array[Byte]], acc: List[FeatureValue] = Nil): IO[List[FeatureValue]] =
    response match {
      case Nil          => IO.pure(acc)
      case null :: tail => decodeResponse(tail, acc)
      case head :: tail =>
        codec.decode(head) match {
          case Left(err)    => IO.raiseError(err)
          case Right(value) => decodeResponse(tail, value :: acc)
        }
    }

  override def close(): Unit = { client.close() }
}

object RedisStore {

  case class RedisKey(ns: Namespace, tenant: Tenant, tag: Tag) {
    val bytes = s"${ns.value}/${tag.scope.name}/${tenant.value}/${tag.value}".getBytes(StandardCharsets.UTF_8)
  }
  object RedisKey {
    def apply(key: Key): RedisKey = new RedisKey(key.ns, key.tenant, key.tag)
    def apply(bytes: Array[Byte]): RedisKey = {
      val tokens = new String(bytes, StandardCharsets.UTF_8).split('/')
      new RedisKey(Namespace(tokens(0)), Tenant(tokens(2)), Tag(Scope(tokens(1)), tokens(3)))
    }
  }

  def makeRedisClient(config: RedisConfig): Resource[IO, Jedis] =
    Resource.make(IO(new Jedis(config.host, config.port)))(client => IO(client.close()))
}
