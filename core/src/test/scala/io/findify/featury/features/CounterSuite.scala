package io.findify.featury.features

import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.{CounterValue, SLong, Timestamp}
import io.findify.featury.model.Write.Increment
import io.findify.featury.utils.TestKey

import scala.util.Random

trait CounterSuite extends FeatureSuite[Increment, CounterValue] {
  val config = CounterConfig(FeatureName("c1"), Namespace("n1"), GroupName("g1"))

  it should "increment once" in {
    val key      = TestKey(id = "p11")
    val result   = write(List(Increment(key, now, 1)))
    val expected = Some(CounterValue(key, now, 1L))
    result shouldBe expected
  }

  it should "inc-dec multiple times" in {
    val key        = TestKey(id = "p12")
    val increments = (0 until 10).map(_ => Increment(key, now, Random.nextInt(100) - 50)).toList
    val result     = write(increments)
    result shouldBe Some(CounterValue(key, now, increments.map(_.inc).sum))
  }

}
