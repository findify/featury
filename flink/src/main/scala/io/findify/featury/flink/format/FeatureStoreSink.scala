package io.findify.featury.flink.format

import io.findify.featury.flink.format.FeatureStoreSink.FeatureValueWriter
import io.findify.featury.model.FeatureValue
import io.findify.featury.values.FeatureStore
import org.apache.flink.api.connector.sink2.{Sink, SinkWriter}

case class FeatureStoreSink(dest: FeatureStore) extends Sink[List[FeatureValue]] {
  override def createWriter(context: Sink.InitContext): SinkWriter[List[FeatureValue]] = FeatureValueWriter(dest)
}

object FeatureStoreSink {
  case class FeatureValueWriter(dest: FeatureStore) extends SinkWriter[List[FeatureValue]] {
    override def write(element: List[FeatureValue], context: SinkWriter.Context): Unit = {
      dest.writeSync(element)
    }

    override def flush(endOfInput: Boolean): Unit = {}

    override def close(): Unit = { dest.closeSync() }
  }
}
