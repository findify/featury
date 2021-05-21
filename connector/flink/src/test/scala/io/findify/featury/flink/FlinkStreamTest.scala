package io.findify.featury.flink

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.Suite

trait FlinkStreamTest { this: Suite =>
  lazy val env: StreamExecutionEnvironment = FlinkStreamTest.stream

}

object FlinkStreamTest {
  lazy val stream = {
    val env = StreamExecutionEnvironment.createLocalEnvironment(1)
    env.setRestartStrategy(RestartStrategies.noRestart())
    env.setParallelism(1)
    env
  }
}
