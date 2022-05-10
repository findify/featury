package io.findify.featury.flink

import better.files.File
import io.findify.featury.flink.FeatureJoinTest.{MerchantScope, ProductScope, SearchScope, UserScope}
import io.findify.featury.flink.format.{BulkCodec, BulkInputFormat}
import io.findify.featury.flink.util.Compress.NoCompression
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.{Key, SString, ScalarState, Schema, State, Timestamp}
import io.findify.featury.utils.TestKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.findify.flinkadt.api._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.contrib.streaming.state.{EmbeddedRocksDBStateBackend, RocksDBStateBackend}
import org.apache.flink.core.fs.Path
import org.apache.flink.state.api.{OperatorTransformation, Savepoint, SavepointWriter}
import scala.language.higherKinds
import scala.concurrent.duration._

class StateBootstrapTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val now = Timestamp.now
  val schema = Schema(
    List(
      ScalarConfig(MerchantScope, FeatureName("lang"), refresh = 0.seconds),
      ScalarConfig(ProductScope, FeatureName("title"), refresh = 0.seconds),
      CounterConfig(SearchScope, FeatureName("count"), refresh = 0.seconds),
      CounterConfig(UserScope, FeatureName("count"), refresh = 0.seconds),
      CounterConfig(ProductScope, FeatureName("clicks"), refresh = 0.seconds)
    )
  )

  it should "write/read state dumps" in {
    val path = File.newTemporaryDirectory("state_test_").deleteOnExit()
    val source = env.fromCollection[State](
      List(
        ScalarState(TestKey(scope = "merchant", fname = "lang", id = "1"), now, SString("foo")),
        ScalarState(TestKey(scope = "merchant", fname = "lang", id = "2"), now, SString("bar"))
      )
    )
    Featury.writeState(source, new Path(path.toString()), NoCompression)
    env.execute()
    path.listRecursively.toList.size should be > 1

    val read =
      Featury.readState(env, new Path(path.toString()), NoCompression, BulkCodec.stateProtobufCodec).javaStream

    val transformation = OperatorTransformation
      .bootstrapWith(read)
      .keyBy[Key](
        new KeySelector[State, Key] {
          override def getKey(value: State): Key = value.key
        },
        implicitly[TypeInformation[Key]]
      )
      .transform(new FeatureBootstrapFunction(schema))

    val savepointPath = File.newTemporaryDirectory("savepoint_tmp_").deleteOnExit()
    SavepointWriter
      .newSavepoint(new EmbeddedRocksDBStateBackend(), 10)
      .withOperator("whatever", transformation)
      .write(s"file://$savepointPath")

    env.execute()
    savepointPath.listRecursively.toList.size should be > 0
  }
}
