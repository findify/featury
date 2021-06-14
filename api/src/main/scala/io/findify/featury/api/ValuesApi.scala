package io.findify.featury.api

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.values.FeatureStore
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import io.circe.syntax._
import org.http4s.circe._

case class ValuesApi(store: FeatureStore) {
  import ValuesApi._
  val service = HttpRoutes.of[IO] {
    case GET -> Root / "status" => Ok("")
    case post @ POST -> Root / "api" / "values" =>
      for {
        read     <- post.as[ReadRequest]
        response <- store.read(read)
        ok       <- Ok(response.asJson)
      } yield {
        ok
      }
  }

}

object ValuesApi {
  implicit val requestDecoder: EntityDecoder[IO, ReadRequest]   = jsonOf
  implicit val responseEncoder: EntityEncoder[IO, ReadResponse] = jsonEncoderOf

}
