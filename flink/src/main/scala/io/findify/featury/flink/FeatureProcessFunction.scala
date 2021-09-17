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
        feature.put(write)
        val lastUpdate = Option(updated.value()).map(ts => Timestamp(ts)).getOrElse(Timestamp(0L))
        if (lastUpdate.diff(write.ts) >= feature.config.refresh) {
          updated.update(write.ts.ts)
          feature
            .computeValue(write.key, write.ts)
            .foreach(value => {
              (value, write) match {
                case (_: ScalarValue, _: Put)                        => // ok
                case (_: NumStatsValue, _: PutStatSample)            => // ok
                case (_: FrequencyValue, _: PutFreqSample)           => // ok
                case (_: CounterValue, _: Increment)                 => // ok
                case (_: PeriodicCounterValue, _: PeriodicIncrement) => // ok
                case (v, w) =>
                  LOG.warn(s"write $w produced value $v")
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
