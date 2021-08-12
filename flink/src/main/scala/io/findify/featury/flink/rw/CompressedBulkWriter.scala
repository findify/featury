package io.findify.featury.flink.rw

import io.findify.featury.flink.util.Compress
import org.apache.flink.api.common.serialization.BulkWriter
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.core.fs.{FSDataOutputStream, Path}
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.{BucketAssigner, OutputFileConfig}
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy

import java.io.BufferedOutputStream

object CompressedBulkWriter {
  def writeFile[T](
      path: Path,
      compress: Compress,
      codec: BulkCodec[T],
      prefix: String
  ) = {
    FileSink
      .forBulkFormat[T](path, CompressedBulkWriterFactory(compress, codec))
      .withRollingPolicy(OnCheckpointRollingPolicy.build())
      .withBucketAssigner(SimpleBucketAssigner(codec))
      .withOutputFileConfig(
        OutputFileConfig.builder().withPartPrefix(prefix).withPartSuffix(".pb" + compress.ext).build()
      )
      .build()
  }

  case class CompressedBulkWriterFactory[T](compress: Compress, codec: BulkCodec[T]) extends BulkWriter.Factory[T] {
    class CompressedBulkWriter(out: BufferedOutputStream) extends BulkWriter[T] {
      override def addElement(element: T): Unit = codec.write(element, out)
      override def flush(): Unit                = out.flush()
      override def finish(): Unit               = out.close()
    }
    override def create(out: FSDataOutputStream): BulkWriter[T] =
      new CompressedBulkWriter(new BufferedOutputStream(compress.write(new NoCloseOutputStream(out)), 10 * 1024))
  }

  case class SimpleBucketAssigner[T](codec: BulkCodec[T]) extends BucketAssigner[T, String] {
    def dispatch(value: T): String                                                = codec.bucket(value)
    override def getSerializer: SimpleVersionedSerializer[String]                 = SimpleVersionedStringSerializer.INSTANCE
    override def getBucketId(element: T, context: BucketAssigner.Context): String = dispatch(element)
  }

}