package io.findify.featury

import org.http4s.HttpRoutes
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.syntax._
import cats.implicits._
import io.findify.featury.config.{ApiConfig, Args}
import io.findify.featury.config.ApiConfig.RedisClientConfig
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.util.ForkJoinExecutor
import io.findify.featury.values.{FeatureStore, RedisStore}
import io.findify.featury.values.StoreCodec.ProtobufCodec
import org.http4s.blaze.server._
import org.http4s.implicits._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  implicit val requestDecoder: EntityDecoder[IO, ReadRequest]   = jsonOf
  implicit val responseEncoder: EntityEncoder[IO, ReadResponse] = jsonEncoderOf
  implicit lazy val ec: ExecutionContext                        = ForkJoinExecutor("api", 4)

  override def run(args: List[String]): IO[ExitCode] = for {
    cmdline <- IO(Args.parse(args))
    config  <- ApiConfig.fromString("")
    result <- config.storeConfig match {
      case redis: RedisClientConfig =>
        RedisStore.makeRedisClient(redis).use(redis => serve(config, RedisStore(redis, ProtobufCodec)))
    }
  } yield {
    result
  }

  def serve(config: ApiConfig, store: FeatureStore)(implicit ec: ExecutionContext) = {
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
    val httpApp = Router("/" -> service).orNotFound

    BlazeServerBuilder[IO](ec)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
