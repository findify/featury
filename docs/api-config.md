# Featury API configuration

*Featury* API is a simple RESTful service made to serve computed feature values to downstream ML models. 
It is designed to be batch-friendly and low-latency. API configuration is defined as a YAML file. Example:

```yaml
store:
  type: memory # can also be redis, cassandra
api:
  host: "0.0.0.0" # host to bind to, default is "0.0.0.0"
  port: 8080 # port exposed by the API, default is 8080
```

Config file is loaded from the following locations, by priority:
1. From the path from command-line args: `--config /path/to/conf/api.yaml`
2. From the system-wide location in `/etc/featury/api.yaml`
3. Fall-back to a default one with in-memory storage


In-memory storage is ephemeral and won't save your data between app restarts. It will also cannot be used
in a distributed setup, as each Featury API node will have its own separate ephemeral storage.

## Redis connector

Redis connector requires some extra configuration:
```yaml
store:
  type: redis
  host: <redis endpoint>
  port: <redis port>
  codec: json # can also be protobuf
```

## Cassandra connector

Cassandra connector also requires some extra config options:
```yaml
store:
  type: cassandra
  hosts: [<host1>, <host2>]
  port: 9042
  dc: datacenter1
  keyspace: <target keyspace name> # Featury will pre-create the keyspace if it's missing
  replication: 3 # RF used when keyspace is created
```
