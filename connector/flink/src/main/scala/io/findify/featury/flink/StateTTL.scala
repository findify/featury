package io.findify.featury.flink

import org.apache.flink.api.common.state.StateTtlConfig
import org.apache.flink.api.common.time.Time

import scala.concurrent.duration.FiniteDuration

object StateTTL {
  def apply(duration: FiniteDuration) = StateTtlConfig
    .newBuilder(Time.seconds(duration.toSeconds))
    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
    .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
    .cleanupFullSnapshot()
    .build
}
