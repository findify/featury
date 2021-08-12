package io.findify.featury.flink

import io.findify.featury.model.{FeatureValue, ScopeKey}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._

/** A temporal join function. See Featury.join for usage.
  *
  * This function receives two streams of events (which are sorted by time!) at the same time. The function
  * is stateful and keyed by the join key. So within the same context (like same ns+scope+tenant+id) it gets:
  * - on a right part, a sorted by time list of feature values. We only keep track of the latest feature value received for
  * each feature.
  * - on a left part is a stream of session events of type T. On each event we grab latest feature values for this event and
  * emit them downstream.
  * @param join
  * @param ki
  * @param vi
  * @tparam T
  */
class FeatureJoinFunction[T](join: Join[T])(implicit
    ki: TypeInformation[String],
    vi: TypeInformation[FeatureValue]
) extends KeyedCoProcessFunction[ScopeKey, T, FeatureValue, T]
    with CheckpointedFunction {

  // to keep track of the latest feature value.
  // map key is feature name
  // the whole state is keyed by a ScopeKey (so  ns+scope+tenant+id)
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
    // just emit all the features we have for current timestamp for this ScopeKey
    val values = lastValues.values().asScala.toList
    out.collect(join.join(value, values))
  }

  override def processElement2(
      value: FeatureValue,
      ctx: KeyedCoProcessFunction[ScopeKey, T, FeatureValue, T]#Context,
      out: Collector[T]
  ): Unit = {
    // replace previous feature value by a new one
    lastValues.put(value.key.name.value, value)
  }

}
