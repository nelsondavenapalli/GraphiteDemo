package fun.abm.GraphiteDemo.metrics;

public interface WithTagsAndMetricName<V extends WithTagsAndMetricName<V>> extends Comparable<V>, WithMetricName, WithTags {
}
