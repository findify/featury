package io.findify.featury.model.json

import io.circe.{Codec, Decoder, Encoder}
import io.findify.featury.model.Timestamp

trait TimestampJson {
  implicit val timestampJson: Codec[Timestamp] = Codec.from(
    decodeA = Decoder.decodeLong.map(Timestamp.apply),
    encodeA = Encoder.encodeLong.contramap(_.ts)
  )
}
