package io.findify.featury.connector.cassandra

import cats.effect.{IO, Resource}
import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, BatchStatement, BatchStatementBuilder, BatchType, Row}
import com.datastax.oss.driver.api.core.{CqlSession, CqlSessionBuilder}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.values.{FeatureStore, StoreCodec}
import io.findify.featury.values.ValueStoreConfig.CassandraConfig
import org.cognitor.cassandra.migration.{Database, MigrationRepository, MigrationTask}
import org.cognitor.cassandra.migration.keyspace.Keyspace
import cats.implicits._

import scala.collection.JavaConverters._
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util

case class CassandraStore(session: CqlSession, codec: StoreCodec) extends FeatureStore {
  lazy val read = session.prepare(
    "select values from features where ns=? and scope=? and tenant=? and id in ? and name in ?"
  )
  lazy val write = session.prepare(
    "insert into features (ns, scope, tenant, name, id, values) values (?,?,?,?,?,?)"
  )
  override def read(request: ReadRequest): IO[ReadResponse] = ???
//  {
//    for {
//      bound <- IO {
//        read.bind(
//          request.ns.value,
//          request.tag.scope.value,
//          request.tenant.value,
//          util.Arrays.asList(request.ids.map(_.value): _*),
//          util.Arrays.asList(request.features.map(_.value): _*)
//        )
//      }
//      rows   <- IO.fromCompletableFuture(IO(session.executeAsync(bound).toCompletableFuture))
//      parsed <- parsePage(rows)
//    } yield {
//      ReadResponse(parsed)
//    }
//  }

  def parsePage(rs: AsyncResultSet, acc: List[FeatureValue] = Nil): IO[List[FeatureValue]] = for {
    values <- rs.currentPage().asScala.toList.traverse(parseRow).map(next => acc ++ next)
    result <-
      if (rs.hasMorePages)
        IO.fromCompletableFuture(IO(rs.fetchNextPage().toCompletableFuture))
          .flatMap(next => parsePage(next, values))
      else
        IO.pure(values)
  } yield {
    result
  }

  def parseRow(row: Row): IO[FeatureValue] = {
    IO.fromEither(codec.decode(row.getByteBuffer("values").array()))
  }

  override def write(batch: List[FeatureValue]): IO[Unit] = ???
//  {
//    val stmt = batch
//      .foldLeft(BatchStatement.builder(BatchType.UNLOGGED))((builder, value) =>
//        builder.addStatement(
//          write.bind(
//            value.key.ns.value,
//            value.key.scope.value,
//            value.key.tenant.value,
//            value.key.name.value,
//            value.key.id.value,
//            ByteBuffer.wrap(codec.encode(value))
//          )
//        )
//      )
//      .build()
//    session.execute(stmt)
//  }

  override def close(): IO[Unit] = IO { session.close() }

}

object CassandraStore {
  def makeResource(conf: CassandraConfig) =
    Resource.make(for {
      _              <- createKeyspace(conf)
      migrateSession <- makeSession(conf)
      _              <- migrate(conf, migrateSession)
      session        <- makeSession(conf)
    } yield {
      new CassandraStore(session, conf.codec)
    })(c => IO(c.session.close()))

  def createKeyspace(conf: CassandraConfig) = IO {
    val builder = CqlSession.builder()
    conf.hosts.foreach(host => builder.addContactPoint(new InetSocketAddress(host, conf.port)))
    val session = builder.withLocalDatacenter(conf.dc).build()
    session.execute(
      s"create keyspace if not exists ${conf.keyspace} with replication = {'class' : 'SimpleStrategy', 'replication_factor' : ${conf.replication}}"
    )
    session.close()
  }

  def makeSession(conf: CassandraConfig) = IO {
    val builder = CqlSession.builder()
    conf.hosts.foreach(host => builder.addContactPoint(new InetSocketAddress(host, conf.port)))
    builder.withLocalDatacenter(conf.dc).withKeyspace(conf.keyspace).build()
  }

  def migrate(conf: CassandraConfig, session: CqlSession): IO[Unit] = IO {
    val db   = new Database(session, new Keyspace(conf.keyspace))
    val task = new MigrationTask(db, new MigrationRepository())
    task.migrate()
  }
}
