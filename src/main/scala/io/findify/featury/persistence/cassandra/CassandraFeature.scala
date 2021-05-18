package io.findify.featury.persistence.cassandra

import cats.effect.IO
import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, Row}
import io.findify.featury.model.Schema.FeatureConfig
import io.findify.featury.model.Timestamp
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._

trait CassandraFeature {
  def ddl(): IO[Unit]
  def tableName[C <: FeatureConfig](cassandra: CassandraConfig, c: C): String =
    s"${cassandra.keyspace}.${c.ns.value}_${c.group.value}_${c.name.value}"

  protected def fetchPages[T](rs: AsyncResultSet, parse: Row => IO[T], acc: List[T] = Nil): IO[List[T]] = {
    if (rs.hasMorePages) {
      for {
        current <- parsePage(rs.currentPage().asScala.toList, parse)
        next    <- IO.fromFuture(IO(rs.fetchNextPage().toScala))
        rec     <- fetchPages(next, parse, current ++ acc)
      } yield {
        rec
      }
    } else {
      parsePage(rs.currentPage().asScala.toList, parse).map(_ ++ acc)
    }
  }

  // recursion over the IO, so it's stack safe
  private def parsePage[T](rows: List[Row], parse: Row => IO[T], acc: List[T] = Nil): IO[List[T]] =
    rows match {
      case Nil => IO.pure(acc)
      case head :: tail =>
        parse(head).flatMap(pair => parsePage(tail, parse, pair :: acc))
    }

}
