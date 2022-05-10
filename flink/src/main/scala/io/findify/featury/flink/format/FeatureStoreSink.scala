package io.findify.featury.flink.format

import cats.effect.unsafe.implicits.global
import io.findify.featury.model.{FeatureValue, FeatureValueMessage}
import io.findify.featury.values.FeatureStore
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}

import scala.jdk.CollectionConverters._

case class FeatureStoreSink(dest: FeatureStore, batchSize: Int)(implicit
    val ti: TypeInformation[FeatureValue],
    iti: TypeInformation[Int],
    lti: TypeInformation[Long]
) extends RichSinkFunction[FeatureValue]
    with CheckpointedFunction {
  @transient var buffer: ListState[FeatureValue] = _
  var size                                       = 0

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val bufferDesc = new ListStateDescriptor[FeatureValue]("write-buf", ti)
    buffer = context.getOperatorStateStore.getListState(bufferDesc)
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  override def invoke(value: FeatureValue, context: SinkFunction.Context): Unit = {
    buffer.add(value)
    size += 1
    if (size >= batchSize) commit()
  }

  override def close(): Unit = {
    commit()
    dest.close()
  }

  private def commit() = {
    val batch = buffer.get().asScala.toList
    if (batch.nonEmpty) {
      dest.write(batch).unsafeRunSync()
      buffer.clear()
      size = 0
    }
  }
}
