package io.findify.featury.values

import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto._
import io.findify.featury.model.BackendError

import scala.util.{Failure, Success}

sealed trait ValueStoreConfig {}

object ValueStoreConfig {
  case class RedisConfig(host: String, port: Int, codec: StoreCodec) extends ValueStoreConfig
  case class MemoryConfig()                                          extends ValueStoreConfig
  case class CassandraConfig(
      hosts: List[String],
      port: Int,
      dc: String,
      keyspace: String,
      codec: StoreCodec,
      replication: Int
  ) extends ValueStoreConfig

}
