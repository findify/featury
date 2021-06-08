package io.findify.featury.config

import cats.effect.IO
import io.findify.featury.config.ApiConfig.{RedisClientConfig, ValueStoreConfig}
import pureconfig._
import pureconfig.generic.semiauto._

case class ApiConfig(storeConfig: ValueStoreConfig)

object ApiConfig {
  sealed trait ValueStoreConfig
  case class RedisClientConfig(host: String, port: Int) extends ValueStoreConfig

  implicit val redisConfigReader = deriveReader[RedisClientConfig]
  implicit val valueStoreReader  = deriveReader[ValueStoreConfig]
  implicit val configReader      = deriveReader[ApiConfig]

  case class ConfigLoadError(err: String) extends Exception(err)

  def fromString(in: String): IO[ApiConfig] = ConfigSource.string(in).load[ApiConfig] match {
    case Left(err)    => IO.raiseError(ConfigLoadError(err.toString()))
    case Right(value) => IO.pure(value)
  }

  def default = ???

}
