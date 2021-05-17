package io.findify.featury.persistence.cassandra

import io.findify.featury.model.Schema.FeatureConfig
import io.findify.featury.persistence.cassandra.CassandraPersistence.CassandraConfig

trait CassandraFeature {
  def tableName[C <: FeatureConfig](cassandra: CassandraConfig, c: C): String =
    s"${cassandra.keyspace}.${c.ns.value}_${c.group.value}_${c.name.value}"
}
