package io.findify.featury.state.codec

import io.findify.featury.state.codec.Codec.DecodingError

import java.io.{InputStream, OutputStream}

trait Codec[T] {
  def encode(value: T, stream: OutputStream): Unit
  def decode(stream: InputStream): Either[DecodingError, T]
}

object Codec {
  case class DecodingError(msg: String) extends Throwable(msg)
}
