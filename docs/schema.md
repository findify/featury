# Feature schema

*Featury* is not a schemaless database and cannot infer feature types
from the data. You need explicitly define feature names, types and options
for each tracked feature.

Schema is defined in a YAML file. Example:
```yaml
features:
  - type: scalar
    ns: dev             
    scope: product       
    name: title
    ttl: '1 day'         # when this feature should expire
    refresh: '0 second'  # how frequently refresh the value
```

## Scoping

*Featury* uses a nested scoping of feature values. So each feature has:
* Namespace: typically an environment, like dev/staging/prod
* Scope: a logical grouping of features sharing the same key. For example,
  a `session` scope may contain all feature values related to the customer 
  session
* name: a feature name. For example, `clicks_count`
* tenant: a tenant identifier for a multi-tenant configuration
* id: key of the feature value. For example, a customer ID.

In RDBMS terms, these scopes can be thought as:
* namespace = database
* scope = table
* name = column
* tenant + id = row primary key

## TTL

Redis and Cassandra connectors support setting a TTL for feature values,
so you can automatically expire ones not being relevant anymore.

## Feature refresh

*Featury* receives a realtime stream of events mutating feature values. But in
some cases it may be an overkill to recompute feature values on each mutation
event. For example, if you track a number of clicks on a specific product, you
may be OK receiving updates to these counters once in an hour. 

While doing offline ML training tasks, you need to perform a temporal join
between your events (like a customer click) and historical feature values 
(like how many clicks this product had). Updating feature values in realtime 
can quickly increase the size of the right part of the join into the sky,
so you may need a lot of storage and CPU to perform it.

Featury allows you to limit the granularity of value updates:
* `refresh` period means that feature value will be updated not earlier
than the period
* decision to refresh the feature value is made on each input message touching
this feature, so there is no guarantee that value update will ever happen.

## Feature configuration

Each feature type has a global shared set of parameters and some unique ones. 
All the tracked features have the following shared parameters:
* type: one of `scalar`,`counter`,`periodic_counter`, `bounded_list`, `freq`, `num_stats` or `map`
* ns: namespace name
* scope: logical grouping of features sharing common key
* name: feature name
* ttl: TTL period
* refresh: minimal value refresh interval

Some feature types also have parameters related to the [feature drift monitoring](metrics.md) like:
* monitorValues: to expose aggregated stats of values in the metrics API.
* monitorLag: expose the feature update lag statistics.
* monitorSize: expose stats about collection sizes.

