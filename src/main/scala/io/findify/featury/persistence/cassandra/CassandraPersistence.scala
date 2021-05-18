package io.findify.featury.persistence.cassandra

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.implicits._
import com.datastax.oss.driver.api.core.{CqlSession, CqlSessionBuilder}
import com.datastax.oss.driver.api.core.session.Session
import io.findify.featury.feature.{BoundedList, Counter, Feature, FreqEstimator, PeriodicCounter, StatsEstimator}
import io.findify.featury.model.FeatureValue
import io.findify.featury.persistence.cassandra.CassandraBoundedList.{CassandraNumBoundedList, CassandraTextBoundedList}
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import io.findify.featury.persistence.{Persistence, ValueStore}
import org.typelevel.log4cats.Logger

import java.net.InetSocketAddress
import java.util.concurrent.{Executor, ExecutorService, Executors}
import scala.concurrent.ExecutionContext
import scala.compat.java8.FutureConverters._

class CassandraPersistence(val session: CqlSession, driverConfig: CassandraConfig)(implicit logger: Logger[IO])
    extends Persistence {
  implicit val threadPool = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  override def periodicCounter(config: PeriodicCounter.PeriodicCounterConfig): IO[PeriodicCounter] =
    makeFeature { new CassandraPeriodicCounter(config, session, driverConfig) }

  override def counter(config: Counter.CounterConfig): IO[Counter] =
    makeFeature { new CassandraCounter(config, session, driverConfig) }

  override def statsEstimator(config: StatsEstimator.StatsEstimatorConfig): IO[StatsEstimator] =
    makeFeature { new CassandraStatsEstimator(config, session, driverConfig) }

  override def numBoundedList(config: BoundedList.BoundedListConfig): IO[BoundedList[FeatureValue.Num]] = {
    makeFeature { new CassandraNumBoundedList(config, session, driverConfig) }
  }

  override def textBoundedList(config: BoundedList.BoundedListConfig): IO[BoundedList[FeatureValue.Text]] =
    makeFeature { new CassandraTextBoundedList(config, session, driverConfig) }

  override def freqEstimator(config: FreqEstimator.FreqEstimatorConfig): IO[FreqEstimator] =
    makeFeature { new CassandraFreqEstimator(config, session, driverConfig) }

  override def values(): IO[ValueStore] = ???

  def makeFeature[F <: Feature[_, _, _] with CassandraFeature](feature: => F): IO[F] = for {
    f <- IO(feature)
    _ <- f.ddl()
  } yield {
    f
  }
}

object CassandraPersistence {
  case class CassandraConfig(endpoints: List[Endpoint], dc: String, keyspace: String)
  case class Endpoint(host: String, port: Int = 9042)
  def resource(config: CassandraConfig)(implicit logger: Logger[IO]) =
    Resource.make(makeSession(config).map(new CassandraPersistence(_, config)))(s => IO(s.session.close()))

  def makeSession(config: CassandraConfig): IO[CqlSession] = {
    val builder = CqlSession.builder()
    config.endpoints.foreach(e => builder.addContactPoint(InetSocketAddress.createUnresolved(e.host, e.port)))
    IO.fromFuture(IO { builder.withLocalDatacenter(config.dc).buildAsync().toScala })
  }
}
