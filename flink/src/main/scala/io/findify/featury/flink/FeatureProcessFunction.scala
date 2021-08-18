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
    features.get(FeatureKey(value.key)) match {
      case None =>
        LOG.warn(s"no features configured for a write $value")
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
      case (f: MapFeature, w: PutTuple)               => f.put(w)
      case (f, w) =>
        LOG.warn(s"received write $w for a feature $f: type mismatch")
    }
}

object FeatureProcessFunction {
  def stateTag(implicit stateTI: TypeInformation[State]) = OutputTag[State]("side-output")
}
