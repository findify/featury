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

import scala.concurrent.duration._

class FeatureValuesTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val path = File.newTemporaryDirectory("valuesink").deleteOnExit()
  val k    = TestKey(id = "p1", fname = "f1")
  val now  = Timestamp.now
  val items = List[FeatureValue](
    ScalarValue(k, now.minus(2.minute), SString("foo")),
    ScalarValue(k, now.minus(1.minute), SString("bar")),
    ScalarValue(k, now, SString("baz"))
  )

  it should "write events to files" in {
    env
      .fromCollection[FeatureValue](items)
      .map(_.asMessage)
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
