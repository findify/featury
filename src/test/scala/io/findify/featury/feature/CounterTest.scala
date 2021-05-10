package io.findify.featury.feature

import io.findify.featury.feature.Counter.{CounterConfig, CounterFeature, Increment}
import io.findify.featury.model.FeatureValue.{Num, ScalarValue}
import io.findify.featury.model.Timestamp
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CounterTest extends AnyFlatSpec with Matchers {
  it should "increment" in {
    val conf  = CounterConfig()
    val start = CounterFeature.emptyState(conf)
    val next  = CounterFeature.update(conf, start, Increment(10, Timestamp.now))
    next.value shouldBe 10.0
    val value = CounterFeature.value(conf, next)
    value shouldBe ScalarValue(Num(10.0))
  }
}
