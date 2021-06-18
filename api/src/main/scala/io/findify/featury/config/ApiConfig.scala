package io.findify.featury.config

import better.files.File
import cats.effect.IO
import io.findify.featury.values.{StoreCodec, ValueStoreConfig}
import pureconfig._
import pureconfig.generic.semiauto._

case class ApiConfig(store: ValueStoreConfig)

object ApiConfig {

  import io.findify.featury.values.ValueStoreConfig._
  implicit val configReader = deriveReader[ApiConfig]

  case class ConfigLoadError(err: String) extends Exception(err)

  def fromString(in: String): IO[ApiConfig] = ConfigSource.string(in).load[ApiConfig] match {
    case Left(err)    => IO.raiseError(ConfigLoadError(err.toString()))
    case Right(value) => IO.pure(value)
  }

  def fromFile(path: String): IO[ApiConfig] = {
    val source = File(path)
    if (source.exists && source.nonEmpty) {
      fromString(source.contentAsString)
    } else {
      IO.raiseError(ConfigLoadError(s"file $path does not exist"))
    }
  }

  def default = ApiConfig(
    store = MemoryConfig()
  )

}
