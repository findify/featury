package io.findify.featury.flink

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.scalatest.{BeforeAndAfterAll, Suite}

trait FlinkStreamTest extends BeforeAndAfterAll { this: Suite =>
  lazy val cluster = new MiniClusterWithClientResource(
    new MiniClusterResourceConfiguration.Builder().setNumberSlotsPerTaskManager(1).setNumberTaskManagers(1).build()
  )

  lazy val env = {
    cluster.getTestEnvironment.setAsContext()
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setRuntimeMode(RuntimeExecutionMode.BATCH)
    env.enableCheckpointing(1000)
    env.setRestartStrategy(RestartStrategies.noRestart())
    //env.getConfig.disableGenericTypes()
    env
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    cluster.before()
  }

  override def afterAll(): Unit = {
    cluster.after()
    super.afterAll()
  }
}

object FlinkStreamTest {}
