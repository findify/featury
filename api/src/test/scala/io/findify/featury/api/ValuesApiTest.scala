package io.findify.featury.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.findify.featury.model.Key.{FeatureName, Id, Namespace, Scope, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.values.MemoryStore
import org.http4s.headers.`Content-Type`
import org.http4s.{Headers, MediaType, Method, Request, Response, Uri}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.syntax._
import io.findify.featury.model.{Key, SString, ScalarValue, Schema, Timestamp}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ValuesApiTest extends AnyFlatSpec with Matchers {
  lazy val store   = new MemoryStore()
  lazy val service = ValuesApi(store, Slf4jLogger.getLogger[IO], MetricsApi(Schema(Nil)))

  lazy val k =
    Key(ns = Namespace("ns"), scope = Scope("s"), tenant = Tenant("t"), name = FeatureName("f1"), id = Id("1"))
  lazy val now = Timestamp.now

  it should "return nil" in {
    val request = ReadRequest(
      ns = Namespace("ns"),
      scope = Scope("s"),
      tenant = Tenant("t"),
      features = List(FeatureName("f")),
      ids = List(Id("a"))
    )
    val result = get(request)
    result.map(_.status.code) shouldBe Some(200)
  }

  it should "return values" in {
    store.cache.put(k, ScalarValue(k, now, SString("foo")))
    val request = ReadRequest(
      ns = Namespace("ns"),
      scope = Scope("s"),
      tenant = Tenant("t"),
      features = List(FeatureName("f1")),
      ids = List(Id("1"))
    )
    val result = get(request)
    result.map(_.status.code) shouldBe Some(200)
    result.map(_.as[ReadResponse].unsafeRunSync()) shouldBe Some(
      ReadResponse(
        List(
          ScalarValue(k, now, SString("foo"))
        )
      )
    )
  }

  def get(request: ReadRequest): Option[Response[IO]] = {
    service.service
      .run(
        Request(
          method = Method.POST,
          headers = Headers(`Content-Type`(MediaType.application.json)),
          body = fs2.Stream[IO, Byte](request.asJson.noSpaces.getBytes: _*),
          uri = Uri.unsafeFromString("/api/values")
        )
      )
      .value
      .unsafeRunSync()
  }
}
