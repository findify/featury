package io.findify.featury.persistence.cassandra

import cats.effect.IO
import com.datastax.oss.driver.api.core.CqlSession
import io.findify.featury.feature.Counter
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.model.Key
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import org.typelevel.log4cats.Logger

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._

class CassandraCounter(val config: CounterConfig, session: CqlSession, cc: CassandraConfig)(implicit logger: Logger[IO])
    extends Counter
    with CassandraFeature {
  val table = tableName(cc, config)

  lazy val incStatement  = session.prepare(s"update $table set value += ? where tenant=? and key=?")
  lazy val readStatement = session.prepare(s"select value from $table where tenant=? and key=?")

  def ddl(): IO[Unit] = IO
    .fromFuture(IO {
      session
        .executeAsync(
          s"create table if not exists $table (tenant int, key text, value counter, primary key ((tenant, key)))"
        )
        .toScala
    })
    .map(_ => {})

  override def increment(key: Key, value: Long): IO[Unit] = for {
    bound <- IO {
      incStatement.bind(java.lang.Long.valueOf(value), java.lang.Integer.valueOf(key.tenant.value), key.id.value)
    }
    _ <- logger.debug(s"incremented [${key.tenant.value},${key.id.value}] by $value")
    _ <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
  } yield {}

  override def readState(key: Key): IO[Counter.CounterState] = for {
    bound   <- IO { readStatement.bind(java.lang.Integer.valueOf(key.tenant.value), key.id.value) }
    results <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    count   <- IO { results.currentPage().asScala.headOption.map(_.getLong("value")) }
  } yield {
    CounterState(count.getOrElse(0L))
  }
}
