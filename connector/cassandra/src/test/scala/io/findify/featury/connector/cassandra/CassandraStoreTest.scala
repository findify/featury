package io.findify.featury.connector.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.values.FeatureStore
import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.findify.featury.values.ValueStoreConfig.CassandraConfig
import org.scalatest.Ignore

@Ignore
class CassandraStoreTest extends StoreTestSuite[CassandraStore] {
  val conf                   = CassandraConfig(List("localhost"), 9042, "datacenter1", "dev", ProtobufCodec, 1)
  override def storeResource = CassandraStore.makeResource(conf)
}
