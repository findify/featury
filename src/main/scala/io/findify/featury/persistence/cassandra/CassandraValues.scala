package io.findify.featury.persistence.cassandra

import cats.effect.IO
import com.datastax.oss.driver.api.core.CqlSession
import io.findify.featury.model.Key.{GroupName, Namespace}
import io.findify.featury.persistence.ValueStore
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig

import scala.compat.java8.FutureConverters._

//class CassandraValues(val session: CqlSession, cc: CassandraConfig, ns: Namespace, group: GroupName)
//    extends ValueStore
//    with CassandraFeature {
//  val table = s"${cc.keyspace}.${ns.value}_${group.value}_values"
//  override def ddl(): IO[Unit] = IO
//    .fromFuture(IO {
//      session
//        .executeAsync(
//          s"""create table if not exists $table
//             |(tenant int, group text, feature text, value text, primary key ((tenant, key), bucket))
//             |""".stripMargin
//        )
//        .toScala
//    })
//    .map(_ => {})
//}
