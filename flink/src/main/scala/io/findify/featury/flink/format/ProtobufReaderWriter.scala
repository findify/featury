package io.findify.featury.flink.format

import io.findify.featury.flink.format.ProtobufReaderWriter.{ProtobufBucketAssigner, ProtobufBulkWriterFactory}
import io.findify.featury.flink.util.Compress
import org.apache.flink.api.common.serialization.BulkWriter
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.file.src.reader.{BulkFormat, StreamFormat}
import org.apache.flink.connector.file.src.{FileSource, FileSourceSplit}
import org.apache.flink.core.fs.{FSDataInputStream, FSDataOutputStream, Path}
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy
import org.apache.flink.streaming.api.functions.sink.filesystem.{BucketAssigner, OutputFileConfig}
import scalapb.GeneratedMessage

import java.io.{BufferedOutputStream, FilterOutputStream, InputStream, OutputStream}

trait ProtobufReaderWriter[T <: GeneratedMessage] {
  def writeFile(
      path: Path,
      bulk: ProtobufBulkWriterFactory[T],
      bucket: ProtobufBucketAssigner[T],
      name: String
  ) = {
    FileSink
      .forBulkFormat[T](path, bulk)
      .withRollingPolicy(OnCheckpointRollingPolicy.build())
      .withBucketAssigner(bucket)
      .withOutputFileConfig(
        OutputFileConfig.builder().withPartPrefix(name).withPartSuffix(".pb" + bulk.compress.ext).build()
      )
      .build()
  }

  def readFile(path: Path, reader: StreamFormat[T]) = {
    FileSource.forRecordStreamFormat[T](reader, path).build()
  }
}

object ProtobufReaderWriter {
  trait ProtobufStreamFormat[T <: GeneratedMessage] extends StreamFormat[T] {
    def ti: TypeInformation[T]
    def compress: Compress
    def make(stream: InputStream): T
    override def getProducedType: TypeInformation[T] = ti

    override def createReader(
        config: Configuration,
        stream: FSDataInputStream,
        fileLen: Long,
        splitEnd: Long
    ): StreamFormat.Reader[T] = ProtobufStreamFormatReader(compress.read(stream), make)

    override def restoreReader(
        config: Configuration,
        stream: FSDataInputStream,
        restoredOffset: Long,
        fileLen: Long,
        splitEnd: Long
    ): StreamFormat.Reader[T] = {
      val compStream = compress.read(stream)
      compStream.skip(restoredOffset)
      ProtobufStreamFormatReader(compStream, make)
    }

    override def isSplittable: Boolean = false
  }
  case class ProtobufStreamFormatReader[T <: GeneratedMessage](
      stream: InputStream,
      make: InputStream => T
  ) extends StreamFormat.Reader[T] {
    override def read(): T     = make(stream)
    override def close(): Unit = stream.close()
  }
  class NoCloseOutputStream(base: OutputStream) extends FilterOutputStream(base) {
    override def close(): Unit = {
      // nope
    }
  }
  trait ProtobufBulkFormat[T <: GeneratedMessage] extends BulkFormat[T, FileSourceSplit] {}

  case class ProtobufBulkWriterFactory[T <: GeneratedMessage](compress: Compress) extends BulkWriter.Factory[T] {
    class ProtobufBulkWriter(out: BufferedOutputStream) extends BulkWriter[T] {
      override def addElement(element: T): Unit = element.writeDelimitedTo(out)
      override def flush(): Unit                = out.flush()
      override def finish(): Unit               = out.close()
    }
    override def create(out: FSDataOutputStream): BulkWriter[T] =
      new ProtobufBulkWriter(new BufferedOutputStream(compress.write(new NoCloseOutputStream(out)), 10 * 1024))
  }

  trait ProtobufBucketAssigner[T <: GeneratedMessage] extends BucketAssigner[T, String] {
    def dispatch(value: T): String
    override def getSerializer: SimpleVersionedSerializer[String]                 = SimpleVersionedStringSerializer.INSTANCE
    override def getBucketId(element: T, context: BucketAssigner.Context): String = dispatch(element)
    override def toString: String                                                 = "EventTypeBucketAssigner"
  }

}
