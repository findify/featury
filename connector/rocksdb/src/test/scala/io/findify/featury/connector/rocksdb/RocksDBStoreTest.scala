package io.findify.featury.connector.rocksdb

import better.files.File
import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.values.FeatureStore
import io.findify.featury.values.StoreCodec.ProtobufCodec

class RocksDBStoreTest extends StoreTestSuite[RocksDBStore] {
  lazy val path = File.newTemporaryDirectory("rocksdb_").deleteOnExit()
  override def storeResource =
    Resource.make(IO(RocksDBStore(path.toString(), ProtobufCodec)))(x => IO(x.close()))
}
