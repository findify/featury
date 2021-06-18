# Monitoring feature drift

*Featury* exports all feature values it served as prometheus metrics on the
`/metrics` endpoint. By default export is disabled and you need to enable it on
a per-feature basis. See the [schema](schema.md) document for details.

These exported metrics can be later used as a source for anomaly detection and dashboarding software:
* monitor feature values quality: what if some feature value becomes missing after update?
* detect value drifts: if historically stable feature suddenly and significantly changes its average value,
it may interfere with the ML model prediction quality.

## Export format

As Featury API service can be distributed (e.g. having multiple Pods in a Deployment in k8s),
all feature values are exported as a [Prometheus Histogram](https://prometheus.io/docs/practices/histograms/#quantiles),
(and not a Summary) so aggregated quantiles can be computed later.

Each feature is exported as a set of separate Prometheus metrics. 

## Example
Here is an example of how a Counter feature value with name `dev_product_clicks` is exposed in the prometheus endpoint:
```
# HELP featury_dev_product_clicks_value histogram of values
# TYPE featury_dev_product_clicks_value histogram
featury_dev_product_clicks_value_bucket{le="0.0",} 0.0
featury_dev_product_clicks_value_bucket{le="0.1",} 0.0
featury_dev_product_clicks_value_bucket{le="0.2",} 0.0
featury_dev_product_clicks_value_bucket{le="0.30000000000000004",} 0.0
featury_dev_product_clicks_value_bucket{le="0.4",} 0.0
featury_dev_product_clicks_value_bucket{le="0.5",} 0.0
featury_dev_product_clicks_value_bucket{le="0.6",} 0.0
featury_dev_product_clicks_value_bucket{le="0.7",} 0.0
featury_dev_product_clicks_value_bucket{le="0.7999999999999999",} 0.0
featury_dev_product_clicks_value_bucket{le="0.8999999999999999",} 0.0
featury_dev_product_clicks_value_bucket{le="0.9999999999999999",} 0.0
featury_dev_product_clicks_value_bucket{le="+Inf",} 1.0
featury_dev_product_clicks_value_count 1.0
featury_dev_product_clicks_value_sum 3.0
```

Prometheus Histogram is a set of bucketed counters, where `le` tag means "number of events with value less than or equal 
to the tag value". Later these counters coming from multiple nodes can be aggregated together into a single percentile
using [histogram_quantile](https://prometheus.io/docs/prometheus/latest/querying/functions/#histogram_quantile) function.


## Scalar feature
* value: histogram of numerical values. String values are not exportable yet.
* lag: seconds passed since last feature value update

## Counter feature
* value: histogram of values
* lag: seconds passed since last feature value update

## Bounded list
* size: histogram of list sizes
* lag: seconds passed since last feature value update

## String frequency
* size: number of unique strings tracked
* lag: seconds passed since last feature value update

## Map feature
* value: histogram of numerical values in the map. String values are not exportable yet.
* size: histogram of map sizes
* lag: seconds passed since last feature value update

## Numerical stats
* value_p<N>: a set of per-quantile histograms.
* lag: seconds passed since last feature value update

## Periodic counter
* value_p<N>: a set of per-period counter histograms.
* lag: seconds passed since last feature value update
