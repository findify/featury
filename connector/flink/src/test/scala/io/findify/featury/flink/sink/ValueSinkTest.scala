package io.findify.featury.flink.sink

import better.files.File
import io.findify.featury.flink.FlinkStreamTest
import io.findify.featury.model.{FeatureValue, SString, ScalarValue, Timestamp}
import io.findify.featury.utils.TestKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path

class ValueSinkTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  it should "write events to files" in {
    val source = List[FeatureValue](
      ScalarValue(TestKey(id = "p1", fname = "f1"), Timestamp.now, SString("foo")),
      ScalarValue(TestKey(id = "p2", fname = "f1"), Timestamp.now, SString("bar")),
      ScalarValue(TestKey(id = "p3", fname = "f1"), Timestamp.now, SString("baz"))
    )

    val path = File.newTemporaryDirectory("valuesink").deleteOnExit()
    env
      .fromCollection[FeatureValue](source)
      .addSink(ValueSink.writeFile(new Path(path.toString())))
    env.execute()
    path.children.isEmpty shouldBe false
  }
}
