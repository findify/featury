package io.findify.featury.flink

import io.findify.featury.flink.format.ProtobufReaderWriter
import io.findify.featury.flink.format.ProtobufReaderWriter.{
  ProtobufBucketAssigner,
  ProtobufBulkWriterFactory,
  ProtobufStreamFormat
}
import io.findify.featury.flink.util.Compress
import io.findify.featury.model.{FeatureKey, FeatureValueMessage}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.core.fs.Path

import java.io.InputStream

object FeatureValues extends ProtobufReaderWriter[FeatureValueMessage] {
  val bucketAssigner = new ProtobufBucketAssigner[FeatureValueMessage] {
    override def dispatch(value: FeatureValueMessage): String = {
      value.toFeatureValue.map(_.key).map(FeatureKey.apply).map(_.fqdn).getOrElse("other")
    }
  }
  case class ValueStreamFormat(compress: Compress)(implicit val ti: TypeInformation[FeatureValueMessage])
      extends ProtobufStreamFormat[FeatureValueMessage] {
    override def make(stream: InputStream): FeatureValueMessage =
      FeatureValueMessage.parseDelimitedFrom(stream).orNull
  }

  def writeFile(path: Path, compress: Compress): FileSink[FeatureValueMessage] = writeFile(
    path = path,
    bulk = new ProtobufBulkWriterFactory[FeatureValueMessage](compress),
    bucket = bucketAssigner,
    name = "values"
  )

  def readFile(path: Path, compress: Compress)(implicit
      ti: TypeInformation[FeatureValueMessage]
  ): FileSource[FeatureValueMessage] =
    readFile(
      path = path,
      reader = ValueStreamFormat(compress)
    )

}
