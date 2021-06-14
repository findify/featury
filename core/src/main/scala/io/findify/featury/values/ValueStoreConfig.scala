package io.findify.featury.values

import io.findify.featury.values.StoreCodec.{JsonCodec, ProtobufCodec}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.FieldCoproductHint
import pureconfig.generic.semiauto.deriveReader

sealed trait ValueStoreConfig {}

object ValueStoreConfig {
  case class RedisConfig(host: String, port: Int, codec: StoreCodec) extends ValueStoreConfig
  case class MemoryConfig()                                          extends ValueStoreConfig

  implicit val animalConfHint = new FieldCoproductHint[ValueStoreConfig]("type") {
    override def fieldValue(name: String) = name.dropRight("Config".length).toLowerCase
  }
  implicit val codecReader = ConfigReader.fromString {
    case "protobuf" => Right(ProtobufCodec)
    case "json"     => Right(JsonCodec)
    case other      => Left(CannotConvert(other, "codec", "not supported"))
  }
  implicit val redisConfigReader = deriveReader[RedisConfig]
  implicit val memConfigReader   = deriveReader[MemoryConfig]
  implicit val valueStoreReader  = deriveReader[ValueStoreConfig]

}
