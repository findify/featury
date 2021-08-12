package io.findify.featury.connector.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.values.FeatureStore
import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.findify.featury.values.ValueStoreConfig.CassandraConfig

class CassandraStoreTest extends StoreTestSuite {
  val conf                                               = CassandraConfig(List("localhost"), 9042, "datacenter1", "dev", ProtobufCodec, 1)
  override def storeResource: Resource[IO, FeatureStore] = CassandraStore.makeResource(conf)
}
