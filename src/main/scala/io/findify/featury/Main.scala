package io.findify.featury

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- IO.unit
  } yield {
    ExitCode.Success
  }
}
