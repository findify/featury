package io.findify.featury.model.json

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser._
import io.circe.syntax._
import io.findify.featury.model.Key._
import io.findify.featury.model.PeriodicCounterValue.PeriodicValue
import io.findify.featury.model._

class FeatureValueJsonTest extends AnyFlatSpec with Matchers {
  import FeatureValueJson._

  val kjson = """{"ns":"dev","scope":"product","tenant":"a","name":"feature","id":"123"}"""
  val k     = Key(Namespace("dev"), Scope("product"), FeatureName("feature"), Tenant("a"), Id("123"))

  it should "decode string scalar" in {
    val result = decode[FeatureValue](s"""{"type":"scalar", "key": $kjson, "ts": 0, "value":"foo"}""")
    result shouldBe Right(ScalarValue(k, Timestamp(0), SString("foo")))
  }

  it should "decode num scalar" in {
    val result = decode[FeatureValue](s"""{"type":"scalar", "key": $kjson, "ts": 0, "value": 123.4}""")
    result shouldBe Right(ScalarValue(k, Timestamp(0), SDouble(123.4)))
  }

  it should "decode counter" in {
    val result = decode[FeatureValue](s"""{"type":"counter", "key": $kjson, "ts": 0, "value": 123}""")
    result shouldBe Right(CounterValue(k, Timestamp(0), 123))
  }

  it should "decode num stats" in {
    val result = decode[FeatureValue](
      s"""{"type":"stats", "key": $kjson, "ts": 0, "min":1.0, "max":2.0, "quantiles":{"50": 1.5}}"""
    )
    result shouldBe Right(NumStatsValue(k, Timestamp(0), 1.0, 2.0, Map(50 -> 1.5)))
  }

  it should "decode periodic counters" in {
    val result = decode[FeatureValue](
      s"""{"type":"periodic_counter", "key": $kjson, "ts": 0, "values": [{"start": 0, "end": 1, "periods":1, "value":1}]}"""
    )
    result shouldBe Right(
      PeriodicCounterValue(
        k,
        Timestamp(0),
        List(PeriodicValue(start = Timestamp(0), end = Timestamp(1), periods = 1, value = 1))
      )
    )
  }

  it should "decode freq" in {
    val result = decode[FeatureValue](s"""{"type":"freq", "key": $kjson, "ts": 0, "values": {"a":1,"b":2}}""")
    result shouldBe Right(FrequencyValue(k, Timestamp(0), Map("a" -> 1, "b" -> 2)))
  }

  it should "decode lists" in {
    val result =
      decode[FeatureValue](s"""{"type":"list", "key": $kjson, "ts": 0, "values": [{"ts": 0, "value": "a"}]}""")
    result shouldBe Right(BoundedListValue(k, Timestamp(0), List(TimeValue(Timestamp(0), SString("a")))))
  }
}
