package io.findify.featury.state.codec
import java.io.{InputStream, OutputStream}
import io.circe.syntax._
import io.circe.parser._
import io.findify.featury.state.codec.Codec.DecodingError

import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

class JsonLineCodec[T](implicit c: io.circe.Codec[T]) extends Codec[T] {
  override def decode(stream: InputStream): Either[Codec.DecodingError, T] = {
    val line = readLine(stream)
    io.circe.parser.decode[T](line) match {
      case Left(value)  => Left(DecodingError(value.toString))
      case Right(value) => Right(value)
    }
  }

  @tailrec private def readLine(stream: InputStream, buffer: StringBuilder = new StringBuilder()): String = {
    val next = stream.read()
    if ((next == -1) || (next == '\n')) {
      buffer.toString()
    } else {
      readLine(stream, buffer.append(Character.toChars(next)))
    }
  }

  override def encode(value: T, stream: OutputStream): Unit = {
    val json = value.asJson.noSpaces
    stream.write(json.getBytes(StandardCharsets.UTF_8))
    stream.write('\n')
  }
}
