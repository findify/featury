package io.findify.featury.config

import better.files.File
import cats.effect.IO
import io.circe.Decoder
import io.findify.featury.values.{StoreCodec, ValueStoreConfig}
import io.circe.generic.semiauto._
import io.circe.yaml.parser.parse
import io.findify.featury.config.ApiConfig.ListenConfig

case class ApiConfig(store: ValueStoreConfig, api: Option[ListenConfig] = None)

object ApiConfig {
  val SYSTEM_CONFIG_PATH = "/etc/featury/api.yaml"

  case class ListenConfig(host: Option[String], port: Option[Int])

  implicit val listenDecoder: Decoder[ListenConfig] = deriveDecoder

  import io.findify.featury.values.ValueStoreConfig._
  implicit val configDecoder = deriveDecoder[ApiConfig]

  case class ConfigLoadError(err: String) extends Exception(err)

  def fromString(in: String): IO[ApiConfig] = parse(in) match {
    case Left(value) => IO.raiseError(ConfigLoadError(value.toString))
    case Right(value) =>
      value.as[ApiConfig] match {
        case Left(value)  => IO.raiseError(ConfigLoadError(value.toString))
        case Right(value) => IO.pure(value)
      }
  }

  def fromFile(path: String): IO[ApiConfig] = {
    val source = File(path)
    if (source.exists && source.nonEmpty) {
      fromString(source.contentAsString)
    } else {
      IO.raiseError(ConfigLoadError(s"file $path does not exist"))
    }
  }

  def system: IO[ApiConfig] = fromFile(SYSTEM_CONFIG_PATH)

  def default = ApiConfig(
    store = MemoryConfig()
  )

}
