# Featury: An online ML feature store
[![License: Apache 2](https://img.shields.io/badge/License-Apache2-green.svg)](https://opensource.org/licenses/Apache-2.0)

Featury is an online ML feature store, built to simplify typical scenarios of online-offline 
ML feature engineering:
* Online API to get latest values of ML features.
* Feature value changes are tracked and persisted into an offline storage (like S3 bucket).
* Historical ML feature values can be joined with training data offline in Spark/Flink.

It differs from existing solutions like Feast/Hopsworks in the following ways:
* Advanced stateful feature types like quantile/frequency estimators, bounded lists 
  and periodic counters.
* Platform-agnostic offline model training: feature value histories are plain CSV/JSON/Protobuf/Parquet
  files, so you can use any tool like Spark/Flink/Pandas to do offline training.
* DB-agnostic for online feature reads: can use Redis/Postgres/Cassandra for persistence.
* Stateless and cloud-native: single jar file for local development, single k8s Deployment for production

## What is feature store

Feature stores are not yet gained a lot of popularity in ML community yet, probably due to the lack of
simple and open-source tools. But the problem solved by feature stores is typical for majority of
production ML system deployments:
* how can you be sure that feature computation is exactly the same while running ML inference online, 
  and training your model offline?
* 