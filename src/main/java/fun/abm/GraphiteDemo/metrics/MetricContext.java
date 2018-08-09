package fun.abm.GraphiteDemo.metrics;

import com.codahale.metrics.Meter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class MetricContext {

  private static final Map<String, MetricContext> ENDPOINTS = new ConcurrentHashMap<>();
  private static final Map<String, MetricContext> METHODS = new ConcurrentHashMap<>();
  public static final String METRIC_NAME_ENDPOINTS = "http-endpoints";
  public static final String METRIC_TAGS_ENDPOINTS_NAME = "endpoint";
  public static final String METRIC_NAME_METHOD_CALL = "method-call";
  public static final String METRIC_TAGS_METHOD_CALL_NAME = "method";

  public static void reset() {
    ENDPOINTS.clear();
    METHODS.clear();
  }

  public static Map<String, String> kv(String key, String value) {
    Map<String, String> tags = new HashMap<>();
    tags.put(key, value);
    return tags;
  }

  public static MetricContext endpoint(MetricRegistryWithTags metricRegistry, String endpointName) {
    return endpoint(metricRegistry, endpointName, null);
  }

  public static MetricContext endpoint(MetricRegistryWithTags metricRegistry, String endpointName, Map<String, String> additionalTags) {
    return ENDPOINTS.computeIfAbsent(endpointName, (key) -> {
      Map<String, String> tags = new HashMap<>();
      if (additionalTags != null) {
        tags.putAll(additionalTags);
      }
      tags.put(METRIC_TAGS_ENDPOINTS_NAME, endpointName);

      MetricContext metricContext = new MetricContext(metricRegistry, METRIC_NAME_ENDPOINTS, tags);
      return metricContext;
    });
  }

  public static MetricContext methodCall(MetricRegistryWithTags metricRegistry, String methodName, Map<String, String> additionalTags) {
    return METHODS.computeIfAbsent(methodName, (key) -> {
      Map<String, String> tags = new HashMap<>(additionalTags);
      tags.put(METRIC_TAGS_METHOD_CALL_NAME, methodName);

      MetricContext metricContext = new MetricContext(metricRegistry, METRIC_NAME_METHOD_CALL + "." + methodName, tags);
      return metricContext;
    });
  }

  TimerWithTags timer;
  Meter failure;
  Meter success;

  public MetricContext(MetricRegistryWithTags metricRegistry, String baseName, Map<String, String> tags) {

    timer = metricRegistry.timerWithTags(baseName, tags);
    failure = metricRegistry.meterWithTags(baseName + ".failure", tags);
    success = metricRegistry.meterWithTags(baseName + ".success", tags);

    metricRegistry.register(baseName + ".timer", timer);
    metricRegistry.register(baseName + ".failure", failure);
    metricRegistry.register(baseName + ".success", success);

  }

  public TimerWithTags.Context start() {
    return timer.time();
  }

  public void incrementFailure() {
    failure.mark();
  }

  public void incrementSuccess() {
    success.mark();
  }

}