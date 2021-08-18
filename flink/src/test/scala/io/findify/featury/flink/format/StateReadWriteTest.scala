package io.findify.featury.flink.format

import better.files.File
import io.findify.featury.flink.util.Compress
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model._
import io.findify.featury.utils.TestKey
import io.findify.flinkadt.api._
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.core.fs.Path
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StateReadWriteTest extends AnyFlatSpec with Matchers with FlinkStreamTest {

  val path = File.newTemporaryDirectory("valuesink").deleteOnExit()
  val k    = TestKey(id = "p1", fname = "f1")
  val now  = Timestamp.now

  val items = List(
    ScalarState(k, now, SString("foo")),
    CounterState(k, now, 1L),
    FrequencyState(k, now, List("foo")),
    StatsState(k, now, List(1.0))
  )

  it should "write events to files" in {
    Featury.writeState(env.fromCollection[State](items), new Path(path.toString()), Compress.ZstdCompression(3))
    env.execute()
    path.children.isEmpty shouldBe false
  }

  it should "read events from files" in {
    val read = Featury
      .readState(batchEnv, new Path(path.toString()), Compress.ZstdCompression(3), BulkCodec.stateProtobufCodec)
      .collect()
    read shouldBe items
  }

}
