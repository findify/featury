package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.flink.util.InitContext
import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.FeatureConfig.{BoundedListConfig, ScalarConfig}
import io.findify.featury.model.{BoundedListState, BoundedListValue, Key, Scalar, TimeValue, Timestamp, Write}
import org.apache.flink.api.common.state.{
  KeyedStateStore,
  ListState,
  ListStateDescriptor,
  ValueState,
  ValueStateDescriptor
}
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.jdk.CollectionConverters._

case class FlinkBoundedList(
    config: BoundedListConfig,
    list: ListState[TimeValue],
    size: ValueState[Int],
    earliest: ValueState[Long]
) extends BoundedList {
  override def put(action: Write.Append): Unit = {
    list.add(TimeValue(action.ts, action.value))
    val updatedSize = Option(size.value()).getOrElse(0) + 1
    size.update(updatedSize)
    if (updatedSize > 2 * config.count) {
      compact(action.ts) match {
        case Some(compacted) =>
          list.update(compacted.asJava)
          size.update(compacted.size)
          earliest.update(compacted.map(_.ts.ts).min)
        case None =>
          list.clear()
          size.update(0)
          earliest.update(action.ts.ts)
      }
    }
  }

  def compact(now: Timestamp): Option[List[TimeValue]] = {
    Option(list.get())
      .map(
        _.asScala.toList
          .filter(_.ts.isAfterOrEquals(now.minus(config.duration)))
          .reverse
          .take(config.count)
      )
  }

  override def computeValue(key: Key, ts: Timestamp): Option[BoundedListValue] = {
    compact(ts).map(items => BoundedListValue(key, ts, items))
  }

  override def writeState(state: BoundedListState): Unit = {
    list.update(state.values.asJava)
    size.update(state.values.size)
    earliest.update(state.values.map(_.ts.ts).min)
  }

  override def readState(key: Key, ts: Timestamp): Option[BoundedListState] =
    compact(ts).map(items => BoundedListState(key, ts, items))
}

object FlinkBoundedList {
  def apply(ctx: InitContext, config: BoundedListConfig)(implicit
      ti: TypeInformation[TimeValue],
      tint: TypeInformation[Int],
      tlong: TypeInformation[Long]
  ): FlinkBoundedList = {
    val desc = new ListStateDescriptor[TimeValue](config.fqdn, ti)
    val ttl  = StateTTL(config.ttl)
    desc.enableTimeToLive(ttl)
    val size = new ValueStateDescriptor[Int](config.fqdn + ".size", tint)
    size.enableTimeToLive(ttl)
    val earliest = new ValueStateDescriptor[Long](config.fqdn + ".earliest", tlong)
    earliest.enableTimeToLive(ttl)
    FlinkBoundedList(config, ctx.getListState(desc), ctx.getState(size), ctx.getState(earliest))
  }
}
