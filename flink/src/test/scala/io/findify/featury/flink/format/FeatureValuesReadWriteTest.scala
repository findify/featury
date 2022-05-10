package io.findify.featury.flink.format

import better.files.File
import io.circe.parser._
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.flink.util.Compress
import io.findify.featury.model.{FeatureValue, SString, ScalarValue, Timestamp}
import io.findify.featury.utils.TestKey
import io.findify.flinkadt.api._
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.core.fs.Path
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.higherKinds

class FeatureValuesReadWriteTest extends AnyFlatSpec with Matchers with FlinkStreamTest {

  val k   = TestKey(id = "p1", fname = "f1")
  val now = Timestamp.now
  val items = List[FeatureValue](
    ScalarValue(k, now.minus(2.minute), SString("foo")),
    ScalarValue(k, now.minus(1.minute), SString("bar")),
    ScalarValue(k, now, SString("baz"))
  )

  it should "write/read events to files" in {
    val path = File.newTemporaryDirectory("valuesink").deleteOnExit()
    Featury.writeFeatures(
      env.fromCollection[FeatureValue](items),
      new Path(path.toString()),
      Compress.ZstdCompression(3)
    )

    env.execute()
    path.children.isEmpty shouldBe false
    val read = env
      .fromSource(
        Featury.readFeatures(new Path(path.toString()), Compress.ZstdCompression(3)),
        WatermarkStrategy.noWatermarks(),
        "read"
      )
      .executeAndCollect(100)
    read shouldBe items
  }

}
