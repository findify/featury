package io.findify.featury.util

import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.{ForkJoinPool, ForkJoinWorkerThread}
import scala.concurrent.ExecutionContext

object ForkJoinExecutor {
  def apply(name: String, parallelism: Int) = {
    val factory = new ForkJoinWorkerThreadFactory() {
      def newThread(pool: ForkJoinPool): ForkJoinWorkerThread = {
        val worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
        worker.setName(s"${name}-${worker.getPoolIndex}")
        worker
      }
    }
    val pool = new ForkJoinPool(parallelism, factory, null, false)
    ExecutionContext.fromExecutorService(pool)
  }
}
