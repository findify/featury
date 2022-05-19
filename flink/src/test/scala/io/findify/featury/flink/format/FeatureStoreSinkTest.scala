package io.findify.featury.flink.format

import cats.effect.unsafe.implicits.global
import io.findify.featury.flink.{FlinkStreamTest, TypeInfoCache}
import io.findify.featury.flink.format.FeatureStoreSinkTest.AggList
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.{FeatureValue, Key, SString, ScalarValue, Timestamp}
import io.findify.featury.values.MemoryStore
import io.findify.flink.api.function.ProcessAllWindowFunction
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.findify.flinkadt.api._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

class FeatureStoreSinkTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  import TypeInfoCache._
  val k   = Key(Tag(Scope("s"), "x1"), FeatureName("f1"), Tenant("1"))
  val now = Timestamp.now

  it should "write to inmem store v2" in {
    val store = MemoryStore()
    val value = ScalarValue(k, now, SString("foo"))
    env
      .fromCollection[FeatureValue](List(value, value, value))
      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(1)))
      .process(new AggList())
      .sinkTo(FeatureStoreSink(store))
    env.execute()
    store.close()
  }
}

object FeatureStoreSinkTest {
  class AggList extends ProcessAllWindowFunction[FeatureValue, List[FeatureValue], TimeWindow] {
    override def process(context: Context, elements: Iterable[FeatureValue], out: Collector[List[FeatureValue]]): Unit =
      out.collect(elements.toList)
  }
}
