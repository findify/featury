package io.findify.featury.flink.rw

import better.files.File
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.flink.{FeatureValues, FlinkStreamTest}
import io.findify.featury.flink.util.Compress
import io.findify.featury.model.{FeatureValue, FeatureValueMessage, Key, SString, ScalarValue, Timestamp}
import io.findify.featury.utils.TestKey
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.flink.core.fs.Path
import io.findify.flinkadt.api._
import io.findify.featury.model.json.FeatureValueJson._
import io.circe.parser._
import scala.language.higherKinds
import scala.concurrent.duration._

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
    env
      .fromCollection[FeatureValue](items)
      .sinkTo(Featury.writeFeatures(new Path(path.toString()), Compress.ZstdCompression(3)))
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

  it should "write events to files in json" in {
    val path = File.newTemporaryDirectory("valuesink").deleteOnExit()
    env
      .fromCollection[FeatureValue](items)
      .sinkTo(
        Featury.writeFeatures(new Path(path.toString()), Compress.NoCompression, BulkCodec.featureValueJsonCodec)
      )
    env.execute()
    val decoded = for {
      child <- path.listRecursively.filterNot(_.isDirectory).toList
      line  <- child.lineIterator.toList
      value <- decode[FeatureValue](line).toOption
    } yield {
      value
    }
    decoded should contain theSameElementsAs items
    path.listRecursively.filterNot(_.isDirectory).toList.forall(_.name.endsWith(".jsonl")) shouldBe true
  }

}
