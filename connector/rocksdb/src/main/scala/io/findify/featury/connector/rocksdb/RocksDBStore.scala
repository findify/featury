package io.findify.featury.connector.rocksdb

import cats.implicits._
import cats.effect.IO
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.model.Key.{Scope, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.values.{FeatureStore, StoreCodec}
import org.rocksdb.{Options, RocksDB}

import java.nio.charset.StandardCharsets

case class RocksDBStore(path: String, codec: StoreCodec) extends FeatureStore {
  @transient lazy val opts = new Options()
    .setCreateIfMissing(true)
    .setCreateMissingColumnFamilies(true)
  @transient lazy val db = RocksDB.open(opts, path)

  override def read(request: ReadRequest): IO[ReadResponse] = {
    val parsed = for {
      tag  <- request.tags
      name <- request.features
      key = keyBytes(Key(tag, name, request.tenant))
      value <- Option(db.get(key))
    } yield {
      codec.decode(value)
    }
    parsed.traverse(x => IO.fromEither(x)).map(ReadResponse.apply)
  }

  override def write(batch: List[FeatureValue]): Unit = for {
    value <- batch
  } yield {
    val key        = keyBytes(Key(value.key.tag, value.key.name, value.key.tenant))
    val valueBytes = codec.encode(value)
    db.put(key, valueBytes)
  }

  override def close(): Unit = {
    db.close()
  }

  def keyBytes(key: Key): Array[Byte] = {
    s"${key.tag.scope.name}/${key.tenant.value}/${key.name.value}/${key.tag.value}"
      .getBytes(StandardCharsets.UTF_8)
  }

}
