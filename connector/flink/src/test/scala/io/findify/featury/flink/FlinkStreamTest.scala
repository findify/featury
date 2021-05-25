package io.findify.featury.flink

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.scalatest.Suite

trait FlinkStreamTest { this: Suite =>
  lazy val env: StreamExecutionEnvironment = FlinkStreamTest.stream

}

object FlinkStreamTest {
  lazy val cluster = new MiniClusterWithClientResource(
    new MiniClusterResourceConfiguration.Builder().setNumberSlotsPerTaskManager(1).setNumberTaskManagers(1).build()
  )
  lazy val stream = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setRestartStrategy(RestartStrategies.noRestart())
    env
  }
}
