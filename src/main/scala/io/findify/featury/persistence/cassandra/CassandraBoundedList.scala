package io.findify.featury.persistence.cassandra

import cats.effect.IO
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.uuid.Uuids
import io.findify.featury.feature.BoundedList
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.feature.FreqEstimator.FreqEstimatorState
import io.findify.featury.model.FeatureValue.{ListItem, Num, NumBoundedListValue, Scalar, Text, TextBoundedListValue}
import io.findify.featury.model.{FeatureValue, Key, Timestamp}
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import org.typelevel.log4cats.Logger

import java.time.Instant
import java.util.UUID
import scala.compat.java8.FutureConverters._
import scala.util.Random

trait CassandraBoundedList[T <: Scalar] extends BoundedList[T] with CassandraFeature {
  def session: CqlSession
  def cc: CassandraConfig
  def encodeValue(value: T): AnyRef
  def decodeValue(row: Row): ListItem[T]
  def logger: Logger[IO]
  def colType: String
  lazy val table = tableName(cc, config)

  lazy val writeStatement = session.prepare(s"update $table set value = ? where tenant=? and key=? and ts=?")
  lazy val readStatement  = session.prepare(s"select value, ts from $table where tenant=? and key=? limit ?")

  override def ddl(): IO[Unit] = IO
    .fromFuture(IO {
      session
        .executeAsync(
          s"""create table if not exists $table
             |(tenant int, key text, ts timeuuid, value $colType, primary key ((tenant, key), ts))
             | with clustering order by (ts desc)""".stripMargin
        )
        .toScala
    })
    .map(_ => {})

  override def put(key: Key, value: T, ts: Timestamp): IO[Unit] = for {
    bound <- IO {
      writeStatement.bind(
        encodeValue(value),
        Integer.valueOf(key.tenant.value),
        key.id.value,
        new UUID(Uuids.startOf(ts.ts).getMostSignificantBits, Uuids.random().getLeastSignificantBits)
      )
    }
    _ <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    _ <- logger.debug(s"wrote [${key.tenant.value},${key.id.value},$ts] = $value")
  } yield {}

  override def readState(key: Key): IO[BoundedList.BoundedListState[T]] = for {
    _         <- logger.debug(s"reading ${config.count} buckets from key [${key.tenant.value},${key.id.value}]")
    bound     <- IO { readStatement.bind(Integer.valueOf(key.tenant.value), key.id.value, Integer.valueOf(config.count)) }
    resultSet <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    samples   <- fetchPages(resultSet, row => IO(decodeValue(row)))
  } yield {
    BoundedListState(samples)
  }
}

object CassandraBoundedList {
  class CassandraTextBoundedList(val config: BoundedListConfig, val session: CqlSession, val cc: CassandraConfig)(
      implicit val logger: Logger[IO]
  ) extends CassandraBoundedList[Text] {
    override def colType = "text"
    override def decodeValue(row: Row): ListItem[Text] =
      ListItem[Text](Text(row.getString("value")), Timestamp(Uuids.unixTimestamp(row.getUuid("ts"))))
    override def encodeValue(value: Text): AnyRef = value.value
    override def fromItems(list: List[ListItem[Text]]): FeatureValue.BoundedListValue[Text] =
      TextBoundedListValue(list)
  }

  class CassandraNumBoundedList(val config: BoundedListConfig, val session: CqlSession, val cc: CassandraConfig)(
      implicit val logger: Logger[IO]
  ) extends CassandraBoundedList[Num] {
    override def colType = "double"
    override def decodeValue(row: Row): ListItem[Num] =
      ListItem[Num](Num(row.getDouble("value")), Timestamp(Uuids.unixTimestamp(row.getUuid("ts"))))
    override def encodeValue(value: Num): AnyRef = java.lang.Double.valueOf(value.value)
    override def fromItems(list: List[ListItem[Num]]): FeatureValue.BoundedListValue[Num] =
      NumBoundedListValue(list)
  }

}
