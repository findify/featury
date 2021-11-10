package io.findify.featury.values

import io.findify.featury.values.StoreCodec.{JsonCodec, ProtobufCodec}
import io.circe.generic.extras.Configuration
import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto.{deriveConfiguredCodec, deriveConfiguredDecoder}
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

  implicit val storeDecoder: Decoder[StoreCodec] = Decoder.decodeString.emapTry {
    case "protobuf" => Success(ProtobufCodec)
    case "json"     => Success(JsonCodec)
    case other      => Failure(BackendError(s"codec $other not supported: try 'json' or 'protobuf'"))
  }
  implicit val redisDecoder: Decoder[RedisConfig]         = deriveDecoder
  implicit val memDecoder: Decoder[MemoryConfig]          = deriveDecoder
  implicit val cassandraDecoder: Decoder[CassandraConfig] = deriveDecoder

  implicit val config = Configuration.default
    .withDiscriminator("type")
    .copy(transformConstructorNames = {
      case "RedisConfig"     => "redis"
      case "MemoryConfig"    => "memory"
      case "CassandraConfig" => "cassandra"
    })

  implicit val vsDecoder: Decoder[ValueStoreConfig] = deriveConfiguredDecoder
}
