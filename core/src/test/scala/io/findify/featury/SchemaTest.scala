package io.findify.featury

import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.Key.{FeatureName, Scope}
import io.findify.featury.model.{FeatureConfig, Schema}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser._

import scala.concurrent.duration._
import scala.util.Success

class SchemaTest extends AnyFlatSpec with Matchers {
  import FeatureConfig._
  it should "parse durations" in {
    decodeDuration("1 days") shouldBe Success(1.day)
    decodeDuration("1 day") shouldBe Success(1.day)
    decodeDuration("1day") shouldBe Success(1.day)
    decodeDuration("1d") shouldBe Success(1.day)
  }
  it should "load scalar config with no drift" in {
    val yaml =
      """
        |features:
        |  - type: scalar
        |    ns: dev
        |    scope: product
        |    name: title
        |    ttl: '1 day'
        |    refresh: '0 second'""".stripMargin
    Schema.fromYaml(yaml) shouldBe Right(
      Schema(
        List(
          ScalarConfig(
            name = FeatureName("title"),
            scope = Scope("product"),
            ttl = 1.day,
            refresh = 0.seconds
          )
        )
      )
    )
  }

  it should "load scalar config with drift" in {
    val yaml =
      """
        |features:
        |  - type: scalar
        |    ns: dev
        |    scope: product
        |    name: title
        |    ttl: '1 day'
        |    refresh: '0 second'
        |    monitorValues:
        |      min: 1.0
        |      max: 2.0
        |      buckets: 10
        |    monitorLag: true""".stripMargin
    Schema.fromYaml(yaml) shouldBe Right(
      Schema(
        List(
          ScalarConfig(
            name = FeatureName("title"),
            scope = Scope("product"),
            ttl = 1.day,
            refresh = 0.seconds,
            monitorValues = Some(MonitorValuesConfig(1.0, 2.0, 10)),
            monitorLag = Some(true)
          )
        )
      )
    )
  }
}
