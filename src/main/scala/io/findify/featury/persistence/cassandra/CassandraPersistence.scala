package io.findify.featury.persistence.cassandra

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.implicits._
import com.datastax.oss.driver.api.core.{CqlSession, CqlSessionBuilder}
import com.datastax.oss.driver.api.core.session.Session
import io.findify.featury.feature.{BoundedList, Counter, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.model.FeatureValue
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import io.findify.featury.persistence.{Persistence, ValueStore}

import java.net.InetSocketAddress
import java.util.concurrent.{Executor, ExecutorService, Executors}
import scala.concurrent.ExecutionContext
import scala.compat.java8.FutureConverters._

class CassandraPersistence(val session: CqlSession, driverConfig: CassandraConfig) extends Persistence {
  implicit val threadPool                                                                          = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  override def periodicCounter(config: PeriodicCounter.PeriodicCounterConfig): IO[PeriodicCounter] = ???

  override def counter(config: Counter.CounterConfig): IO[Counter] = for {
    counter <- IO { new CassandraCounter(config, session, driverConfig) }
    _       <- counter.ddl()
  } yield {
    counter
  }

  override def statsEstimator(config: StatsEstimator.StatsEstimatorConfig): IO[StatsEstimator] = ???

  override def numBoundedList(config: BoundedList.BoundedListConfig): IO[BoundedList[FeatureValue.Num]] = ???

  override def textBoundedList(config: BoundedList.BoundedListConfig): IO[BoundedList[FeatureValue.Text]] = ???

  override def freqEstimator(config: FreqEstimator.FreqEstimatorConfig): IO[FreqEstimator] = ???

  override def values(): IO[ValueStore] = ???
}

object CassandraPersistence {
  case class CassandraConfig(endpoints: List[Endpoint], dc: String, keyspace: String)
  case class Endpoint(host: String, port: Int = 9042)
  def resource(config: CassandraConfig) =
    Resource.make(makeSession(config).map(new CassandraPersistence(_, config)))(s => IO(s.session.close()))

  def makeSession(config: CassandraConfig): IO[CqlSession] = {
    val builder = CqlSession.builder()
    config.endpoints.foreach(e => builder.addContactPoint(InetSocketAddress.createUnresolved(e.host, e.port)))
    IO.fromFuture(IO { builder.withLocalDatacenter(config.dc).buildAsync().toScala })
  }
}
