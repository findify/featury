package io.findify.featury.values

import cats.effect.{IO, Resource}
import io.findify.featury.values.StoreCodec.ProtobufCodec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import redis.clients.jedis.Jedis

class RedisStoreTest extends StoreTestSuite {
  override def storeResource: Resource[IO, FeatureStore] =
    Resource.make(IO(RedisStore(new Jedis("localhost", 6379), ProtobufCodec)))(redis => IO(redis.client.close()))
}
