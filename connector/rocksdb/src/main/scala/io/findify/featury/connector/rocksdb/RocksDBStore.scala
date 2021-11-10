package io.findify.featury.connector.rocksdb

import cats.implicits._
import cats.effect.IO
import io.findify.featury.model.{FeatureValue, Key}
import io.findify.featury.model.Key.{Scope, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.values.{FeatureStore, StoreCodec}
import org.rocksdb.{Options, RocksDB}

import java.nio.charset.StandardCharsets

case class RocksDBStore(path: String, codec: StoreCodec, readOnly: Boolean) extends FeatureStore {
  @transient lazy val opts = new Options()
    .setCreateIfMissing(true)
    .setCreateMissingColumnFamilies(true)

  @transient lazy val db = if (readOnly) RocksDB.openReadOnly(opts, path) else RocksDB.open(opts, path)

  override def read(request: ReadRequest): IO[ReadResponse] = {
    val parsed = for {
      key <- request.keys
      keyb = keyBytes(key)
      value <- Option(db.get(keyb))
    } yield {
      codec.decode(value)
    }
    parsed.traverse(x => IO.fromEither(x)).map(ReadResponse.apply)
  }
  override def write(batch: List[FeatureValue]): IO[Unit] =
    if (readOnly) IO.raiseError(new IllegalAccessError()) else writeDo(batch)

  private def writeDo(batch: List[FeatureValue]): IO[Unit] = IO {
    for {
      value <- batch
    } yield {
      val key        = keyBytes(Key(value.key.tag, value.key.name, value.key.tenant))
      val valueBytes = codec.encode(value)
      db.put(key, valueBytes)
    }
  }

  override def close() = IO {
    db.close()
  }

  def keyBytes(key: Key): Array[Byte] = {
    s"${key.tag.scope.name}/${key.tenant.value}/${key.name.value}/${key.tag.value}"
      .getBytes(StandardCharsets.UTF_8)
  }

}
