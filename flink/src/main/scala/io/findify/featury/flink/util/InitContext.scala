package io.findify.featury.flink.util

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.state.{
  KeyedStateStore,
  ListState,
  ListStateDescriptor,
  MapState,
  MapStateDescriptor,
  ValueState,
  ValueStateDescriptor
}
import org.apache.flink.runtime.state.FunctionInitializationContext

/** Flink has two separate flows of context initialization:
  * 1. DataStream one, which assumes that your stateful operator extends CheckpointedFunction and
  * overrides a method `initializeState`. This is the most typical way of dealing with state.
  * 2. Legacy DataSet one, where you do `getRuntimeContext` in the `open()`. Used in state processor API.
  *
  * Both DataStream's KeyedStateStore and DataSet's RuntimeContext have exactly the same set of methods
  * to init the state, but they lack a common interface, so we manually dispatch over them here.
  */
sealed trait InitContext {
  def getState[T](desc: ValueStateDescriptor[T]): ValueState[T]
  def getMapState[K, V](desc: MapStateDescriptor[K, V]): MapState[K, V]
  def getListState[T](desc: ListStateDescriptor[T]): ListState[T]
}

object InitContext {
  case class DataStreamContext(store: KeyedStateStore) extends InitContext {
    override def getState[T](desc: ValueStateDescriptor[T]): ValueState[T]         = store.getState(desc)
    override def getMapState[K, V](desc: MapStateDescriptor[K, V]): MapState[K, V] = store.getMapState(desc)
    override def getListState[T](desc: ListStateDescriptor[T]): ListState[T]       = store.getListState(desc)
  }
  object DataStreamContext {
    def apply(ctx: FunctionInitializationContext) = new DataStreamContext(ctx.getKeyedStateStore)
  }
  case class DataSetContext(ctx: RuntimeContext) extends InitContext {
    override def getState[T](desc: ValueStateDescriptor[T]): ValueState[T]         = ctx.getState(desc)
    override def getMapState[K, V](desc: MapStateDescriptor[K, V]): MapState[K, V] = ctx.getMapState(desc)
    override def getListState[T](desc: ListStateDescriptor[T]): ListState[T]       = ctx.getListState(desc)
  }
}
