package fun.abm.GraphiteDemo.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

public class MetricRegistryWithTags extends MetricRegistry implements WithTags {

  @Getter
  private Map<String, String> tags = new HashMap<>();
  @Getter
  private final String namespace;
  private Map<CounterWithTags, CounterWithTags> countersWithTagsMap = new ConcurrentHashMap<>();
  private Map<HistogramWithTags, HistogramWithTags> histogramsWithTagsMap = new ConcurrentHashMap<>();
  private Map<MeterWithTags, MeterWithTags> metersWithTagsMap = new ConcurrentHashMap<>();
  private Map<TimerWithTags, TimerWithTags> timersWithTagsMap = new ConcurrentHashMap<>();
  private Map<GaugeWithTags<?>, GaugeWithTags<?>> gaugesWithTagsMap = new ConcurrentHashMap<>();

  public MetricRegistryWithTags(String namespace) {
    this.namespace = namespace;
  }

  public Set<CounterWithTags> getCountersWithTags() {
    return countersWithTagsMap.keySet();
  }

  public CounterWithTags counterWithTags(String metricName, Map<String, String> tags) {
    CounterWithTags counter = new CounterWithTags(metricName, tags);
    countersWithTagsMap.putIfAbsent(counter, counter);
    return countersWithTagsMap.get(counter);
  }

  public Set<GaugeWithTags<?>> getGaugesWithTags() {
    return gaugesWithTagsMap.keySet();
  }

  @SuppressWarnings("unchecked")
  public <T> GaugeWithTags<T> gaugeWithTags(String metricName, Map<String, String> tags, Gauge<T> gauge) {
    GaugeWithTags<T> counter = new GaugeWithTags<>(metricName, tags, gauge);
    gaugesWithTagsMap.putIfAbsent(counter, counter);
    return (GaugeWithTags<T>) gaugesWithTagsMap.get(counter);
  }

  public Set<HistogramWithTags> getHistogramsWithTags() {
    return histogramsWithTagsMap.keySet();
  }

  public HistogramWithTags histogramWithTags(String metricName, Map<String, String> tags, Reservoir reservoir) {
    HistogramWithTags histogram = new HistogramWithTags(metricName, tags, reservoir);
    histogramsWithTagsMap.putIfAbsent(histogram, histogram);
    return histogramsWithTagsMap.get(histogram);
  }

  public Set<MeterWithTags> getMetersWithTags() {
    return metersWithTagsMap.keySet();
  }

  public MeterWithTags meterWithTags(String metricName, Map<String, String> tags) {
    MeterWithTags meter = new MeterWithTags(metricName, tags);
    metersWithTagsMap.putIfAbsent(meter, meter);
    return metersWithTagsMap.get(meter);
  }

  public Set<TimerWithTags> getTimersWithTags() {
    return timersWithTagsMap.keySet();
  }

  public TimerWithTags timerWithTags(String metricName, Map<String, String> tags) {
    TimerWithTags timer = new TimerWithTags(metricName, tags);
    timersWithTagsMap.putIfAbsent(timer, timer);
    return timersWithTagsMap.get(timer);
  }

}
