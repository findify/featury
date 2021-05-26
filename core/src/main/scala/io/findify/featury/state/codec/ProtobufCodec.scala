package io.findify.featury.state.codec

import io.findify.featury.state.codec.Codec.DecodingError
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import java.io.{InputStream, OutputStream}

class ProtobufCodec[T <: GeneratedMessage](companion: GeneratedMessageCompanion[T]) extends Codec[T] {
  override def decode(stream: InputStream): Either[Codec.DecodingError, T] =
    companion.parseDelimitedFrom(stream) match {
      case Some(value) => Right(value)
      case None        => Left(DecodingError("cannot decode message"))
    }

  override def encode(value: T, stream: OutputStream): Unit = value.writeDelimitedTo(stream)
}
