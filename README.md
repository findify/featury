# Featury: An online ML feature store
[![CI Status](https://github.com/findify/featury/workflows/CI/badge.svg)](https://github.com/findify/featury/actions)
[![License: Apache 2](https://img.shields.io/badge/License-Apache2-green.svg)](https://opensource.org/licenses/Apache-2.0)
![Last commit](https://img.shields.io/github/last-commit/findify/featury)
![Last release](https://img.shields.io/github/release/findify/featury)

Featury is an end-to-end framework, built to simplify typical scenarios of online-offline ML feature engineering:
* Online API to serve latest values of ML features.
* Feature value changes are tracked and persisted into an offline storage (HDFS or S3 bucket).
* Historical ML feature values can be joined with training data offline in Spark/Flink to do model training,
  feature boostrapping and offline evaluation.

It differs from existing solutions like Feast/Hopsworks in the following ways:
* Featury not only handles get-set actions for feature values, but can do stateful processing:
  * increments for counters and periodic counters
  * string frequency sampling
  * numerical stats estimation for median, average and percentiles.
  * bounded lists and maps.
* Platform-agnostic offline model training: feature value histories are plain CSV/JSON/Protobuf/Parquet
  files, so you can use any tool like Spark/Flink/Pandas to do offline training.
* DB-agnostic for online feature reads: can use Redis/Postgres/Cassandra for persistence.
* Stateless and cloud-native: single jar file for local development, single k8s Deployment for production


## Featury is not (only) a feature store

The problem solved by feature stores is typical for majority of production ML system deployments:
* how can you be sure that feature computation is exactly the same while running ML inference online, 
  and training your model offline?
* feature values drift in time, and while doing offline training, you may need to get access to a historical
  values of the feature.
* new features require bootstrapping, so you should be able not only to compute them now, but also for all historical
  actions back in time
* while doing online inference, you may need to access hundreds of features for hundreds of items with low latency

![Data flow](docs/img/data_flow.svg)

**Featury** tries to solve these problems by encapsulating feature value tracking task:
* it logs all feature value changes, so for any feature you have full historical view of changes.
* Write operations in **Featury** are extremely fast and happening in the background.
* Feature values are eventually recomputed (like computing running median over a sampled reservoir) and exposed
  in inference API.
  
## License

Featury is licensed under the Apache 2.0.