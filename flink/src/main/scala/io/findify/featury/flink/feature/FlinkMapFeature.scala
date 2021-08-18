package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.flink.util.InitContext
import io.findify.featury.model
import io.findify.featury.model.Feature.MapFeature
import io.findify.featury.model.FeatureConfig.{MapConfig, ScalarConfig}
import io.findify.featury.model.{Key, MapValue, Scalar, Timestamp, Write}
import org.apache.flink.api.common.state.{KeyedStateStore, MapState, MapStateDescriptor, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.collection.JavaConverters._

case class FlinkMapFeature(config: MapConfig, mapState: MapState[String, Scalar]) extends MapFeature {
  override def put(action: Write.PutTuple): Unit = {
    action.value match {
      case Some(value) => mapState.put(action.mapKey, value)
      case None        => mapState.remove(action.mapKey)
    }
  }

  override def writeState(state: model.MapState): Unit = {
    state.values.foreach { case (key, value) =>
      mapState.put(key, value)
    }
  }

  override def readState(key: Key, ts: Timestamp): Option[model.MapState] = {
    val values = mapState.entries().asScala.map(e => e.getKey -> e.getValue).toList
    if (values.isEmpty) {
      None
    } else {
      Some(model.MapState(key, ts, values.toMap))
    }
  }

  override def computeValue(key: Key, ts: Timestamp): Option[MapValue] = {
    val values = mapState.entries().asScala.map(e => e.getKey -> e.getValue).toList
    if (values.isEmpty) {
      None
    } else {
      Some(MapValue(key, ts, values.toMap))
    }
  }
}

object FlinkMapFeature {
  def apply(ctx: InitContext, config: MapConfig)(implicit
      ti: TypeInformation[Scalar],
      si: TypeInformation[String]
  ): FlinkMapFeature = {
    val desc = new MapStateDescriptor[String, Scalar](config.fqdn, si, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    new FlinkMapFeature(config, ctx.getMapState(desc))
  }

}
