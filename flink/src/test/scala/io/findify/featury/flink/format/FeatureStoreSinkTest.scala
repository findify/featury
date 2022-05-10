package io.findify.featury.flink.format

import cats.effect.unsafe.implicits.global
import io.findify.featury.flink.FlinkStreamTest
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.model.{FeatureValue, Key, SString, ScalarValue, Timestamp}
import io.findify.featury.values.MemoryStore
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.findify.flinkadt.api._

class FeatureStoreSinkTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val k   = Key(Tag(Scope("s"), "x1"), FeatureName("f1"), Tenant("1"))
  val now = Timestamp.now

  it should "write to inmem store" in {
    val store = MemoryStore()
    val value = ScalarValue(k, now, SString("foo"))
    env
      .fromCollection[FeatureValue](List(value))
      .addSink(FeatureStoreSink(store, 100))
    env.execute()
    store.close()
  }
}
