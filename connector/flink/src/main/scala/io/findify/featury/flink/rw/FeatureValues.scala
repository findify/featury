package io.findify.featury.flink.rw

import io.findify.featury.flink.rw.ProtobufReaderWriter.{
  ProtobufBucketAssigner,
  ProtobufBulkWriterFactory,
  ProtobufStreamFormat
}
import io.findify.featury.flink.util.Compress
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
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.core.fs.Path

import java.io.InputStream

object FeatureValues extends ProtobufReaderWriter[FeatureValue] {
  val bucketAssigner = new ProtobufBucketAssigner[FeatureValue] {
    override def dispatch(value: FeatureValue): String = value match {
      case ScalarValue(key, _, _)          => FeatureKey(key).fqdn
      case CounterValue(key, _, _)         => FeatureKey(key).fqdn
      case NumStatsValue(key, _, _, _, _)  => FeatureKey(key).fqdn
      case PeriodicCounterValue(key, _, _) => FeatureKey(key).fqdn
      case FrequencyValue(key, _, _)       => FeatureKey(key).fqdn
      case BoundedListValue(key, _, _)     => FeatureKey(key).fqdn
      case _                               => "other"
    }
  }
  case class ValueStreamFormat(compress: Compress)(implicit val ti: TypeInformation[FeatureValue])
      extends ProtobufStreamFormat[FeatureValue] {
    override def make(stream: InputStream): FeatureValue =
      FeatureValueMessage.parseDelimitedFrom(stream).map(_.toFeatureValue).orNull
  }

  def writeFile(path: Path, compress: Compress): FileSink[FeatureValue] = writeFile(
    path = path,
    bulk = new ProtobufBulkWriterFactory[FeatureValue](compress),
    bucket = bucketAssigner,
    name = "values"
  )

  def readFile(path: Path, compress: Compress)(implicit ti: TypeInformation[FeatureValue]): FileSource[FeatureValue] =
    readFile(
      path = path,
      reader = ValueStreamFormat(compress)
    )

}
