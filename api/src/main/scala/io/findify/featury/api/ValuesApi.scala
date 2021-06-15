package io.findify.featury.api

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.values.FeatureStore
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import io.circe.syntax._
import org.http4s.circe._
import org.typelevel.log4cats.Logger

case class ValuesApi(store: FeatureStore, logger: Logger[IO]) {
  import ValuesApi._
  val service = HttpRoutes.of[IO] {
    case GET -> Root / "status" => Ok("")
    case post @ POST -> Root / "api" / "values" =>
      for {
        read <- post.as[ReadRequest]
        _ <- logger.debug(
          s"received request: ns=${read.ns} scope=${read.scope} tenant=${read.tenant} features=${read.features
            .map(_.value)} ids=${read.ids.map(_.value)}"
        )
        response <- store.read(read)
        _        <- logger.debug(s"read ${response.features.size} values")
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
