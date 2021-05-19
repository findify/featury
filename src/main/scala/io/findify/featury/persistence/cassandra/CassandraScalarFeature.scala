package io.findify.featury.persistence.cassandra

import cats.effect.IO
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.uuid.Uuids
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.feature.ScalarFeature
import io.findify.featury.feature.ScalarFeature.{ScalarConfig, ScalarState}
import io.findify.featury.model.FeatureValue.{ListItem, Num, NumScalarValue, Scalar, ScalarValue, Text, TextScalarValue}
import io.findify.featury.model.{Key, Timestamp}
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.compat.java8.FutureConverters._

trait CassandraScalarFeature[T <: Scalar] extends ScalarFeature[T] with CassandraFeature {
  def logger: Logger[IO]
  def session: CqlSession
  def cc: CassandraConfig
  def encodeValue(value: T): AnyRef
  def decodeValue(row: Row): ScalarState[T]
  def colType: String

  lazy val table = tableName(cc, config)

  lazy val writeStatement = session.prepare(s"update $table set value = ? where tenant=? and key=?")
  lazy val readStatement  = session.prepare(s"select value from $table where tenant=? and key=?")

  def ddl(): IO[Unit] = IO
    .fromFuture(IO {
      session
        .executeAsync(
          s"create table if not exists $table (tenant int, key text, value $colType, primary key ((tenant, key)))"
        )
        .toScala
    })
    .map(_ => {})

  override def put(key: Key, value: T): IO[Unit] = for {
    bound <- IO {
      writeStatement.bind(
        encodeValue(value),
        Integer.valueOf(key.tenant.value),
        key.id.value
      )
    }
    _ <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    _ <- logger.debug(s"wrote [${key.tenant.value},${key.id.value}] = $value")
  } yield {}

  override def readState(key: Key): IO[Option[ScalarState[T]]] = for {
    _         <- logger.debug(s"reading value from key [${key.tenant.value},${key.id.value}]")
    bound     <- IO { readStatement.bind(Integer.valueOf(key.tenant.value), key.id.value) }
    resultSet <- IO.fromFuture(IO { session.executeAsync(bound).toScala })
    samples   <- fetchPages(resultSet, row => IO(decodeValue(row)))
  } yield {
    samples.headOption
  }

}

object CassandraScalarFeature {
  case class CassandraTextScalarFeature(
      config: ScalarConfig,
      session: CqlSession,
      cc: CassandraConfig
  )(implicit val logger: Logger[IO])
      extends CassandraScalarFeature[Text] {
    override val colType                                  = "text"
    override def encodeValue(value: Text): AnyRef         = value.value
    override def decodeValue(row: Row): ScalarState[Text] = ScalarState(Text(row.getString("value")))
    override def computeValue(state: ScalarFeature.ScalarState[Text]): Option[ScalarValue[Text]] = Some(
      TextScalarValue(state.value)
    )
  }

  case class CassandraNumScalarFeature(
      config: ScalarConfig,
      session: CqlSession,
      cc: CassandraConfig
  )(implicit val logger: Logger[IO])
      extends CassandraScalarFeature[Num] {
    override val colType                                 = "double"
    override def encodeValue(value: Num): AnyRef         = java.lang.Double.valueOf(value.value)
    override def decodeValue(row: Row): ScalarState[Num] = ScalarState(Num(row.getDouble("value")))
    override def computeValue(state: ScalarFeature.ScalarState[Num]): Option[ScalarValue[Num]] = Some(
      NumScalarValue(state.value)
    )
  }
}
