package fun.abm.GraphiteDemo.metrics;

public class CompareUtil {

  public static boolean equals(WithTagsAndMetricName<?> metric, Object obj) {
    if (metric == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (metric.getClass() != obj.getClass()) {
      return false;
    }

    WithTagsAndMetricName<?> other = (WithTagsAndMetricName<?>) obj;

    if (metric.getMetricName() == null) {
      if (other.getMetricName() != null) {
        return false;
      }
    } else if (!metric.getMetricName().equals(other.getMetricName())) {
      return false;
    }

    if (metric.getTags() == null) {
      if (other.getTags() != null) {
        return false;
      }
    } else if (!metric.getTags().equals(other.getTags())) {
      return false;
    }
    return true;
  }

}
