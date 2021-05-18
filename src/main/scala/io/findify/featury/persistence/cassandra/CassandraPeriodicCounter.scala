package io.findify.featury.persistence.cassandra

import cats.effect.IO
import cats.effect.IO.{IOCont, Uncancelable}
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, Row}
import io.findify.featury.feature.Counter.CounterState
import io.findify.featury.feature.PeriodicCounter
import io.findify.featury.feature.PeriodicCounter.{PeriodicCounterConfig, PeriodicCounterState}
import io.findify.featury.model.{Key, Timestamp}
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import org.typelevel.log4cats.Logger

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId, ZoneOffset}
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._

class CassandraPeriodicCounter(val config: PeriodicCounterConfig, session: CqlSession, cc: CassandraConfig)(implicit
    logger: Logger[IO]
) extends PeriodicCounter
    with CassandraFeature {
  lazy val table = tableName(cc, config)

  lazy val incStatement  = session.prepare(s"update $table set value += ? where tenant=? and key=? and period=?")
  lazy val readStatement = session.prepare(s"select period, value from $table where tenant=? and key=? limit ?")

  def ddl(): IO[Unit] = IO
    .fromFuture(IO {
      session
        .executeAsync(
          s"""create table if not exists $table
            |(tenant int, key text, period timestamp, value counter, primary key ((tenant, key), period))
            |with clustering order by (period desc)""".stripMargin
        )
        .toScala
    })
    .map(_ => {})

  override def increment(key: Key, ts: Timestamp, value: Long): IO[Unit] = {
    val date = Instant.ofEpochMilli(ts.toStartOfPeriod(config.period).ts)
    for {
      bound <- IO {
        incStatement.bind(
          java.lang.Long.valueOf(value),
          java.lang.Integer.valueOf(key.tenant.value),
          key.id.value,
          date
        )
      }
      _ <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
      _ <- logger.debug(s"incremented [${key.tenant.value},${key.id.value},${date.toString}] by $value")
    } yield {}
  }

  override def readState(key: Key): IO[PeriodicCounter.PeriodicCounterState] = {
    val periods = config.earliestPeriodOffset + 1
    for {
      _         <- logger.debug(s"reading last $periods from key [${key.tenant.value},${key.id.value}]")
      bound     <- IO { readStatement.bind(Integer.valueOf(key.tenant.value), key.id.value, Integer.valueOf(periods)) }
      resultSet <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
      counters  <- fetchPages(resultSet, parseRow)
    } yield {
      PeriodicCounterState(filterCounters(counters).toMap)
    }
  }

  private def parseRow(row: Row): IO[(Timestamp, Long)] = for {
    period <- IO { row.getInstant("period") }
    count  <- IO { row.getLong("value") }
    ts     <- IO { Timestamp(period.toEpochMilli) }
  } yield {
    ts -> count
  }

  private def filterCounters(counters: List[(Timestamp, Long)]) = if (counters.nonEmpty) {
    val last  = counters.map(_._1).maxBy(_.ts)
    val first = last.minus(config.period * config.earliestPeriodOffset)
    counters.filter(_._1.isAfterOrEquals(first))
  } else {
    Nil
  }

}
