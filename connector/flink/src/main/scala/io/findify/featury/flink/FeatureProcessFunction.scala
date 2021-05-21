package io.findify.featury.flink

import io.findify.featury.flink.FeatureProcessFunction.KeyedFeatureValue
import io.findify.featury.model.{Feature, FeatureConfig, FeatureValue, Key, Timestamp, Write}
import io.findify.featury.model.Key.FeatureKey
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.{TypeInfo, TypeInformation}
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.apache.flink.api.scala._

class FeatureProcessFunction[W <: Write, T <: FeatureValue, C <: FeatureConfig, F <: Feature[W, T, C]](
    configs: Map[FeatureKey, C],
    name: String,
    make: (KeyedStateStore, C) => F
) extends KeyedProcessFunction[Key, W, KeyedFeatureValue[T]]
    with CheckpointedFunction {

  @transient var features: Map[FeatureKey, F] = _
  @transient var updated: ValueState[Long]    = _

  override def initializeState(context: FunctionInitializationContext): Unit = {
    features = configs.map { case (key, config) =>
      key -> make(context.getKeyedStateStore, config)
    }
    updated =
      context.getKeyedStateStore.getState(new ValueStateDescriptor[Long](name, implicitly[TypeInformation[Long]]))
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  override def processElement(
      value: W,
      ctx: KeyedProcessFunction[Key, W, KeyedFeatureValue[T]]#Context,
      out: Collector[KeyedFeatureValue[T]]
  ): Unit = {
    features.get(ctx.getCurrentKey.featureKey) match {
      case None =>
      // wtf?
      case Some(feature) =>
        feature.put(value)
        val lastUpdate = Option(updated.value()).map(ts => Timestamp(ts)).getOrElse(Timestamp(0L))
        if (lastUpdate.diff(value.ts) > feature.config.ttl) {
          updated.update(value.ts.ts)
          feature.computeValue(value.key).foreach(value => out.collect(KeyedFeatureValue(ctx.getCurrentKey, value)))
        }
    }
  }
}

object FeatureProcessFunction {
  case class KeyedFeatureValue[T <: FeatureValue](key: Key, value: T)
}
