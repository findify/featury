package io.findify.featury.flink

import io.findify.featury.flink.FeatureProcessFunction.stateTag
import io.findify.featury.flink.feature.{
  FlinkBoundedList,
  FlinkCounter,
  FlinkFreqEstimator,
  FlinkPeriodicCounter,
  FlinkScalarFeature,
  FlinkStatsEstimator
}
import io.findify.featury.model.Feature.{
  BoundedList,
  Counter,
  FreqEstimator,
  PeriodicCounter,
  ScalarFeature,
  StatsEstimator
}
import io.findify.featury.model.FeatureConfig.{
  BoundedListConfig,
  CounterConfig,
  FreqEstimatorConfig,
  PeriodicCounterConfig,
  ScalarConfig,
  StatsEstimatorConfig
}
import io.findify.featury.model.Write.{Append, Increment, PeriodicIncrement, Put, PutFreqSample, PutStatSample}
import io.findify.featury.model.{
  Feature,
  FeatureConfig,
  FeatureKey,
  FeatureValue,
  FeatureValueMessage,
  Key,
  Schema,
  State,
  Timestamp,
  Write
}
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.{TypeInfo, TypeInformation}
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

class FeatureProcessFunction(schema: Schema)
    extends KeyedProcessFunction[Key, Write, FeatureValue]
    with CheckpointedFunction {

  @transient var features: Map[FeatureKey, Feature[_ <: Write, _ <: FeatureValue, _ <: FeatureConfig, _ <: State]] = _
  @transient var updated: ValueState[Long]                                                                         = _

  override def initializeState(context: FunctionInitializationContext): Unit = {
    features = schema.configs.map {
      case (key, c: CounterConfig)         => key -> FlinkCounter(context.getKeyedStateStore, c)
      case (key, c: PeriodicCounterConfig) => key -> FlinkPeriodicCounter(context.getKeyedStateStore, c)
      case (key, c: BoundedListConfig)     => key -> FlinkBoundedList(context.getKeyedStateStore, c)
      case (key, c: FreqEstimatorConfig)   => key -> FlinkFreqEstimator(context.getKeyedStateStore, c)
      case (key, c: ScalarConfig)          => key -> FlinkScalarFeature(context.getKeyedStateStore, c)
      case (key, c: StatsEstimatorConfig)  => key -> FlinkStatsEstimator(context.getKeyedStateStore, c)
    }
    updated = context.getKeyedStateStore.getState(
      new ValueStateDescriptor[Long]("last-update", implicitly[TypeInformation[Long]])
    )
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  override def processElement(
      value: Write,
      ctx: KeyedProcessFunction[Key, Write, FeatureValue]#Context,
      out: Collector[FeatureValue]
  ): Unit = {
    features.get(FeatureKey(ctx.getCurrentKey)) match {
      case None =>
      // wtf?
      case Some(feature) =>
        putWrite(feature, value)
        val lastUpdate = Option(updated.value()).map(ts => Timestamp(ts)).getOrElse(Timestamp(0L))
        if (lastUpdate.diff(value.ts) >= feature.config.refresh) {
          updated.update(value.ts.ts)
          feature
            .computeValue(value.key, value.ts)
            .foreach(value => {
              out.collect(value)
            })
          feature.readState(value.key, value.ts).foreach(state => ctx.output(stateTag, state))
        }
    }
  }

  private def putWrite(feature: Feature[_ <: Write, _ <: FeatureValue, _ <: FeatureConfig, _ <: State], write: Write) =
    (feature, write) match {
      case (f: Counter, w: Increment)                 => f.put(w)
      case (f: BoundedList, w: Append)                => f.put(w)
      case (f: FreqEstimator, w: PutFreqSample)       => f.put(w)
      case (f: PeriodicCounter, w: PeriodicIncrement) => f.put(w)
      case (f: ScalarFeature, w: Put)                 => f.put(w)
      case (f: StatsEstimator, w: PutStatSample)      => f.put(w)
      case _                                          => // ignore
    }
}

object FeatureProcessFunction {
  val stateTag = OutputTag[State]("side-output")
}
