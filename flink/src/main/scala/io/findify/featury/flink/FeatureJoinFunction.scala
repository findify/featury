package io.findify.featury.flink

import io.findify.featury.flink.util.{Item, ScopeKey}
import io.findify.featury.model.FeatureValue
import org.apache.flink.api.common.functions.{CoGroupFunction, RichCoGroupFunction}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector
import scala.collection.JavaConverters._

class FeatureJoinFunction[T](append: (T, List[FeatureValue]) => T)(implicit
    ki: TypeInformation[String],
    vi: TypeInformation[FeatureValue]
) extends KeyedCoProcessFunction[ScopeKey, T, FeatureValue, T]
    with CheckpointedFunction {

  var lastValues: MapState[String, FeatureValue] = _

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val desc = new MapStateDescriptor[String, FeatureValue]("last", ki, vi)
    lastValues = context.getKeyedStateStore.getMapState(desc)
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  override def processElement1(
      value: T,
      ctx: KeyedCoProcessFunction[ScopeKey, T, FeatureValue, T]#Context,
      out: Collector[T]
  ): Unit = {
    val values = lastValues.values().asScala.toList
    if (values.nonEmpty) {
      out.collect(append(value, values))
    }
    val br = 1
  }

  override def processElement2(
      value: FeatureValue,
      ctx: KeyedCoProcessFunction[ScopeKey, T, FeatureValue, T]#Context,
      out: Collector[T]
  ): Unit = {
    lastValues.put(value.key.name.value, value)
    val br = 1
  }

}
