package io.findify.featury.flink

import io.findify.featury.flink.FeatureProcessFunction.stateTag
import io.findify.featury.flink.feature._
import io.findify.featury.flink.util.InitContext.DataStreamContext
import io.findify.featury.model.Feature._
import io.findify.featury.model.FeatureConfig._
import io.findify.featury.model.Write._
import io.findify.featury.model._
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.{TypeInfo, TypeInformation}
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import org.slf4j.LoggerFactory

/** A function to map interactions to features defined in schema. See Featury.process for overview.
  * @param schema
  */
class FeatureProcessFunction(schema: Schema)(implicit
    longTI: TypeInformation[Long],
    intTI: TypeInformation[Int],
    doubleTI: TypeInformation[Double],
    tvTI: TypeInformation[TimeValue],
    stringTI: TypeInformation[String],
    scalarTI: TypeInformation[Scalar],
    stateTI: TypeInformation[State]
) extends KeyedProcessFunction[Key, Write, FeatureValue]
    with CheckpointedFunction
    with StatefulFeatureFunction {

  private val LOG = LoggerFactory.getLogger(classOf[FeatureProcessFunction])

  override def initializeState(context: FunctionInitializationContext): Unit = {
    init(schema, DataStreamContext(context))
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  override def processElement(
      value: Write,
      ctx: KeyedProcessFunction[Key, Write, FeatureValue]#Context,
      out: Collector[FeatureValue]
  ): Unit = {
    value match {
      case w: Put               => putWrite(scalars.get(FeatureKey(value.key)), w, ctx, out)
      case w: PutTuple          => putWrite(maps.get(FeatureKey(value.key)), w, ctx, out)
      case w: Increment         => putWrite(counters.get(FeatureKey(value.key)), w, ctx, out)
      case w: PeriodicIncrement => putWrite(periodicCounters.get(FeatureKey(value.key)), w, ctx, out)
      case w: Append            => putWrite(lists.get(FeatureKey(value.key)), w, ctx, out)
      case w: PutStatSample     => putWrite(stats.get(FeatureKey(value.key)), w, ctx, out)
      case w: PutFreqSample     => putWrite(freqs.get(FeatureKey(value.key)), w, ctx, out)
    }
  }

  private def putWrite[W <: Write, T <: FeatureValue](
      featureOption: Option[Feature[W, T, _ <: FeatureConfig, _ <: State]],
      write: W,
      ctx: KeyedProcessFunction[Key, Write, FeatureValue]#Context,
      out: Collector[FeatureValue]
  ) = {
    featureOption match {
      case None =>
        LOG.warn(s"no features configured for a write $write")
      case Some(feature) =>
        schema.configs.get(FeatureKey(write.key)) match {
          case Some(conf) => // ok
          case None       => LOG.warn(s"config missing for write $write")
        }
        (feature, write) match {
          case (feature: ScalarFeature, w: Put)                 => // ok
          case (feature: MapFeature, w: PutTuple)               => // ok
          case (counter: Counter, w: Increment)                 => // ok
          case (list: BoundedList, w: Append)                   => // ok
          case (estimator: FreqEstimator, w: PutFreqSample)     => // ok
          case (counter: PeriodicCounter, w: PeriodicIncrement) => // ok
          case (estimator: StatsEstimator, w: PutStatSample)    => // ok
          case (f, w) =>
            LOG.warn(s"type mismatch: feature=$f write=$w")
        }
        feature.put(write)
        val lastUpdate = Option(updated.value()).map(ts => Timestamp(ts)).getOrElse(Timestamp(0L))
        if (lastUpdate.diff(write.ts) >= feature.config.refresh) {
          updated.update(write.ts.ts)
          feature
            .computeValue(write.key, write.ts)
            .foreach(value => {
              (value, write, schema.configs.get(FeatureKey(write.key))) match {
                case (v: ScalarValue, w: Put, Some(c: ScalarConfig))
                    if (w.key == v.key) && (c.name == v.key.name) && (c.scope == v.key.tag.scope) && (c.name == v.key.name) => // ok
                case (v: NumStatsValue, w: PutStatSample, Some(c: StatsEstimatorConfig))
                    if (w.key == v.key) && (c.name == v.key.name) && (c.scope == v.key.tag.scope) && (c.name == v.key.name) => // ok
                case (v: FrequencyValue, w: PutFreqSample, Some(c: FreqEstimatorConfig))
                    if (w.key == v.key) && (c.name == v.key.name) && (c.scope == v.key.tag.scope) && (c.name == v.key.name) => // ok
                case (v: CounterValue, w: Increment, Some(c: CounterConfig))
                    if (w.key == v.key) && (c.name == v.key.name) && (c.scope == v.key.tag.scope) && (c.name == v.key.name) => // ok
                case (v: PeriodicCounterValue, w: PeriodicIncrement, Some(c: PeriodicCounterConfig))
                    if (w.key == v.key) && (c.name == v.key.name) && (c.scope == v.key.tag.scope) && (c.name == v.key.name) => // ok
                case (v: BoundedListValue, w: Append, Some(c: BoundedListConfig))
                    if (w.key == v.key) && (c.name == v.key.name) && (c.scope == v.key.tag.scope) && (c.name == v.key.name) => // ok
                case (v, w, c) =>
                  LOG.warn(s"write $w produced value $v for config $c")
              }
              out.collect(value)
            })
          feature.readState(write.key, write.ts).foreach(state => ctx.output(stateTag, state))
        }
    }
  }
}

object FeatureProcessFunction {
  def stateTag(implicit stateTI: TypeInformation[State]) = OutputTag[State]("side-output")
}
