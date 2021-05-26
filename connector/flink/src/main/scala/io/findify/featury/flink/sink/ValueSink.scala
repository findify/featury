package io.findify.featury.flink.sink

import com.github.luben.zstd.ZstdOutputStream
import io.findify.featury.model.{
  BoundedListValue,
  CounterValue,
  FeatureKey,
  FeatureValue,
  FeatureValueMessage,
  FrequencyValue,
  NumStatsValue,
  PeriodicCounterValue,
  ScalarValue,
  State
}
import org.apache.flink.api.common.serialization.{BulkWriter, Encoder}
import org.apache.flink.core.fs.{FSDataOutputStream, Path}
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.{BucketAssigner, OutputFileConfig, StreamingFileSink}
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.{
  CheckpointRollingPolicy,
  OnCheckpointRollingPolicy
}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, GeneratedSealedOneof}

import java.io.{BufferedOutputStream, FilterOutputStream, OutputStream}

object ValueSink {
  class NoCloseOutputStream(base: OutputStream) extends FilterOutputStream(base) {
    override def close(): Unit = {
      // nope
    }
  }
  case object ValueBulkWriterFactory extends BulkWriter.Factory[FeatureValue] {
    class ValueBulkWriter(out: BufferedOutputStream) extends BulkWriter[FeatureValue] {
      override def addElement(element: FeatureValue): Unit = element.asMessage.writeDelimitedTo(out)
      override def flush(): Unit                           = out.flush()
      override def finish(): Unit                          = out.close()
    }
    override def create(out: FSDataOutputStream): BulkWriter[FeatureValue] =
      new ValueBulkWriter(new BufferedOutputStream(new ZstdOutputStream(new NoCloseOutputStream(out)), 10 * 1024))
  }

  case object ValueTypeBucketAssigner extends BucketAssigner[FeatureValue, String] {
    override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE

    override def getBucketId(element: FeatureValue, context: BucketAssigner.Context): String = {
      element.asNonEmpty match {
        case Some(value) =>
          value match {
            case ScalarValue(key, _, _)          => FeatureKey(key).fqdn
            case CounterValue(key, _, _)         => FeatureKey(key).fqdn
            case NumStatsValue(key, _, _, _, _)  => FeatureKey(key).fqdn
            case PeriodicCounterValue(key, _, _) => FeatureKey(key).fqdn
            case FrequencyValue(key, _, _)       => FeatureKey(key).fqdn
            case BoundedListValue(key, _, _)     => FeatureKey(key).fqdn
          }
        case None => "other"
      }
    }

    override def toString: String = "EventTypeBucketAssigner"
  }

  def writeFile(path: Path, roll: CheckpointRollingPolicy[FeatureValue, String] = OnCheckpointRollingPolicy.build()) = {
    StreamingFileSink
      .forBulkFormat[FeatureValue](path, ValueBulkWriterFactory)
      .withRollingPolicy(roll)
      .withBucketAssigner(ValueTypeBucketAssigner)
      .withOutputFileConfig(OutputFileConfig.builder().withPartPrefix("values").withPartSuffix(".pb.zst").build())
      .build()
  }

  private def protobuf[T <: GeneratedSealedOneof]: Encoder[T] =
    (element: T, stream: OutputStream) => element.asMessage.writeDelimitedTo(stream)

}
