package io.findify.featury.features

import io.findify.featury.model.FeatureConfig.MapConfig
import io.findify.featury.model.Key.{FeatureName, Namespace, Scope}
import io.findify.featury.model.{MapValue, SString, ScalarValue}
import io.findify.featury.model.Write.{Put, PutTuple}
import io.findify.featury.utils.TestKey

import scala.concurrent.duration._

trait MapFeatureSuite extends FeatureSuite[PutTuple] {
  val config = MapConfig(ns = Namespace("a"), group = Scope("b"), FeatureName("counter"), 1.day)
  val k      = TestKey(config, id = "p11")

  it should "write-read" in {
    val result = write(List(PutTuple(k, now, "foo", Some(SString("bar")))))
    result shouldBe Some(MapValue(k, now, Map("foo" -> SString("bar"))))
  }

  it should "update" in {
    val result = write(List(PutTuple(k, now, "foo", Some(SString("baz")))))
    result shouldBe Some(MapValue(k, now, Map("foo" -> SString("baz"))))
  }

  it should "remove" in {
    val result = write(List(PutTuple(k, now, "foo", None)))
    result shouldBe None
  }
}
