package io.findify.featury.persistence.cassandra

import cats.effect.IO
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.Row
import io.findify.featury.feature.StatsEstimator
import io.findify.featury.feature.StatsEstimator.{StatsEstimatorConfig, StatsEstimatorState}
import io.findify.featury.model.Key
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import org.typelevel.log4cats.Logger

import scala.compat.java8.FutureConverters._
import scala.util.Random

class CassandraStatsEstimator(val config: StatsEstimatorConfig, session: CqlSession, cc: CassandraConfig)(implicit
    logger: Logger[IO]
) extends StatsEstimator
    with CassandraFeature {
  lazy val table = tableName(cc, config)

  lazy val incStatement  = session.prepare(s"update $table set value = ? where tenant=? and key=? and bucket=?")
  lazy val readStatement = session.prepare(s"select value from $table where tenant=? and key=?")

  def ddl(): IO[Unit] = IO
    .fromFuture(IO {
      session
        .executeAsync(
          s"""create table if not exists $table
             |(tenant int, key text, bucket int, value double, primary key ((tenant, key), bucket))
             |""".stripMargin
        )
        .toScala
    })
    .map(_ => {})

  override def putReal(key: Key, value: Double): IO[Unit] = for {
    bucket <- IO { Random.nextInt(config.poolSize) }
    bound <- IO {
      incStatement.bind(
        java.lang.Double.valueOf(value),
        Integer.valueOf(key.tenant.value),
        key.id.value,
        Integer.valueOf(bucket)
      )
    }
    _ <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    _ <- logger.debug(s"wrote [${key.tenant.value},${key.id.value},$bucket] = $value")
  } yield {}

  override def readState(key: Key): IO[StatsEstimator.StatsEstimatorState] = for {
    _         <- logger.debug(s"reading ${config.poolSize} buckets from key [${key.tenant.value},${key.id.value}]")
    bound     <- IO { readStatement.bind(Integer.valueOf(key.tenant.value), key.id.value) }
    resultSet <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    samples   <- fetchPages(resultSet, parseRow)
  } yield {
    StatsEstimatorState(samples.toVector)
  }

  private def parseRow(row: Row): IO[Double] = IO { row.getDouble("value") }

}
