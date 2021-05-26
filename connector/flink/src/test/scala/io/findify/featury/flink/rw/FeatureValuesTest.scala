package io.findify.featury.flink.rw

import better.files.File
import io.findify.featury.flink.FlinkStreamTest
import io.findify.featury.flink.util.Compress
import io.findify.featury.model.{FeatureValue, SString, ScalarValue, Timestamp}
import io.findify.featury.utils.TestKey
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path

class FeatureValuesTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val path = File.newTemporaryDirectory("valuesink").deleteOnExit()
  val items = List[FeatureValue](
    ScalarValue(TestKey(id = "p1", fname = "f1"), Timestamp.now, SString("foo")),
    ScalarValue(TestKey(id = "p2", fname = "f1"), Timestamp.now, SString("bar")),
    ScalarValue(TestKey(id = "p3", fname = "f1"), Timestamp.now, SString("baz"))
  )

  it should "write events to files" in {
    env
      .fromCollection[FeatureValue](items)
      .sinkTo(FeatureValues.writeFile(new Path(path.toString()), Compress.ZstdCompression(3)))
    env.execute()
    path.children.isEmpty shouldBe false
  }

  it should "read events from files" in {
    val read = env
      .fromSource(
        FeatureValues.readFile(new Path(path.toString()), Compress.ZstdCompression(3)),
        WatermarkStrategy.noWatermarks(),
        "read"
      )
      .executeAndCollect(100)
    read shouldBe items
  }
}
