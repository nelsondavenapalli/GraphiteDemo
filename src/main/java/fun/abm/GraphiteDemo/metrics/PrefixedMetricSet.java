package fun.abm.GraphiteDemo.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import java.util.HashMap;
import java.util.Map;

public class PrefixedMetricSet implements MetricSet {

  private MetricSet metricSet;
  private String prefix;

  public PrefixedMetricSet(String prefix, MetricSet metricSet) {
    this.prefix = prefix;
    this.metricSet = metricSet;
  }

  @Override
  public Map<String, Metric> getMetrics() {
    Map<String, Metric> metrics = new HashMap<>();

    metricSet.getMetrics().forEach((name, metric) -> metrics.put(prefix + '.' + name, metric));

    return metrics;
  }

}
