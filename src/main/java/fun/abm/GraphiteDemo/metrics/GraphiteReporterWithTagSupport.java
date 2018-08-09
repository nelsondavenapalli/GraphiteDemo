package fun.abm.GraphiteDemo.metrics;

import static com.codahale.metrics.MetricAttribute.COUNT;
import static com.codahale.metrics.MetricAttribute.M15_RATE;
import static com.codahale.metrics.MetricAttribute.M1_RATE;
import static com.codahale.metrics.MetricAttribute.M5_RATE;
import static com.codahale.metrics.MetricAttribute.MAX;
import static com.codahale.metrics.MetricAttribute.MEAN;
import static com.codahale.metrics.MetricAttribute.MEAN_RATE;
import static com.codahale.metrics.MetricAttribute.MIN;
import static com.codahale.metrics.MetricAttribute.P50;
import static com.codahale.metrics.MetricAttribute.P75;
import static com.codahale.metrics.MetricAttribute.P95;
import static com.codahale.metrics.MetricAttribute.P98;
import static com.codahale.metrics.MetricAttribute.P99;
import static com.codahale.metrics.MetricAttribute.P999;
import static com.codahale.metrics.MetricAttribute.STDDEV;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteSender;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphiteReporterWithTagSupport extends ScheduledReporter {

  /**
   * Returns a new {@link Builder} for {@link GraphiteReporterWithTagSupport}.
   *
   * @param registry the registry to report
   * @return a {@link Builder} instance for a {@link GraphiteReporterWithTagSupport}
   */
  public static Builder forRegistry(MetricRegistry registry) {
      return new Builder(registry);
  }

  /**
   * A builder for {@link GraphiteReporterWithTagSupport} instances. Defaults to not using a prefix, using the
   * default clock, converting rates to events/second, converting durations to milliseconds, and
   * not filtering metrics.
   */
  public static class Builder {
      private final MetricRegistry registry;
      private Clock clock;
      private String prefix;
      private TimeUnit rateUnit;
      private TimeUnit durationUnit;
      private MetricFilter filter;
      private ScheduledExecutorService executor;
      private boolean shutdownExecutorOnStop;
      private Set<MetricAttribute> disabledMetricAttributes;

      private Builder(MetricRegistry registry) {
          this.registry = registry;
          this.clock = Clock.defaultClock();
          this.prefix = null;
          this.rateUnit = TimeUnit.SECONDS;
          this.durationUnit = TimeUnit.MILLISECONDS;
          this.filter = MetricFilter.ALL;
          this.executor = null;
          this.shutdownExecutorOnStop = true;
          this.disabledMetricAttributes = Collections.emptySet();
      }

      /**
       * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
       * Default value is true.
       * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
       *
       * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
       * @return {@code this}
       */
      public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
          this.shutdownExecutorOnStop = shutdownExecutorOnStop;
          return this;
      }

      /**
       * Specifies the executor to use while scheduling reporting of metrics.
       * Default value is null.
       * Null value leads to executor will be auto created on start.
       *
       * @param executor the executor to use while scheduling reporting of metrics.
       * @return {@code this}
       */
      public Builder scheduleOn(ScheduledExecutorService executor) {
          this.executor = executor;
          return this;
      }

      /**
       * Use the given {@link Clock} instance for the time.
       *
       * @param clock a {@link Clock} instance
       * @return {@code this}
       */
      public Builder withClock(Clock clock) {
          this.clock = clock;
          return this;
      }

      /**
       * Prefix all metric names with the given string.
       *
       * @param prefix the prefix for all metric names
       * @return {@code this}
       */
      public Builder prefixedWith(String prefix) {
          this.prefix = prefix;
          return this;
      }

      /**
       * Convert rates to the given time unit.
       *
       * @param rateUnit a unit of time
       * @return {@code this}
       */
      public Builder convertRatesTo(TimeUnit rateUnit) {
          this.rateUnit = rateUnit;
          return this;
      }

      /**
       * Convert durations to the given time unit.
       *
       * @param durationUnit a unit of time
       * @return {@code this}
       */
      public Builder convertDurationsTo(TimeUnit durationUnit) {
          this.durationUnit = durationUnit;
          return this;
      }

      /**
       * Only report metrics which match the given filter.
       *
       * @param filter a {@link MetricFilter}
       * @return {@code this}
       */
      public Builder filter(MetricFilter filter) {
          this.filter = filter;
          return this;
      }

      /**
       * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
       * See {@link MetricAttribute}.
       *
       * @param disabledMetricAttributes a {@link MetricFilter}
       * @return {@code this}
       */
      public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
          this.disabledMetricAttributes = disabledMetricAttributes;
          return this;
      }

      /**
       * Builds a {@link GraphiteReporterWithTagSupport} with the given properties, sending metrics using the
       * given {@link GraphiteSender}.
       *
       * Present for binary compatibility
       *
       * @param graphite a {@link Graphite}
       * @return a {@link GraphiteReporterWithTagSupport}
       */
      public GraphiteReporterWithTagSupport build(Graphite graphite) {
          return build((GraphiteSender) graphite);
      }

      /**
       * Builds a {@link GraphiteReporterWithTagSupport} with the given properties, sending metrics using the
       * given {@link GraphiteSender}.
       *
       * @param graphite a {@link GraphiteSender}
       * @return a {@link GraphiteReporterWithTagSupport}
       */
      public GraphiteReporterWithTagSupport build(GraphiteSender graphite) {
          return new GraphiteReporterWithTagSupport(
              registry,
              graphite,
              clock,
              prefix,
              rateUnit,
              durationUnit,
              filter,
              executor,
              shutdownExecutorOnStop,
              disabledMetricAttributes);
      }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphiteReporterWithTagSupport.class);

  private MetricRegistry metricRegistry;
  private GraphiteSender graphite;
  private String prefix;
  private Clock clock;

  public GraphiteReporterWithTagSupport(
      MetricRegistry metricRegistry,
      GraphiteSender graphite,
      Clock clock,
      String prefix,
      TimeUnit rateUnit,
      TimeUnit durationUnit,
      MetricFilter filter,
      ScheduledExecutorService executor,
      boolean shutdownExecutorOnStop,
      Set<MetricAttribute> disabledMetricAttributes) {
    super(metricRegistry, "graphite-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
    this.graphite = graphite;
    this.clock = clock;
    this.prefix = prefix;
    this.metricRegistry = metricRegistry;

    if(metricRegistry instanceof MetricRegistryWithTags) {
      if (this.prefix == null) {
        this.prefix = ((MetricRegistryWithTags) metricRegistry).getNamespace();
      } else {
        this.prefix += '.' + ((MetricRegistryWithTags) metricRegistry).getNamespace();
      }
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void report(SortedMap<String, Gauge> gauges,
                     SortedMap<String, Counter> counters,
                     SortedMap<String, Histogram> histograms,
                     SortedMap<String, Meter> meters,
                     SortedMap<String, Timer> timers) {
      Set<CounterWithTags> countersWithTags = null;
      Set<GaugeWithTags<?>> gaugesWithTags = null;
      Set<HistogramWithTags> histogramsWithTags = null;
      Set<MeterWithTags> metersWithTags = null;
      Set<TimerWithTags> timersWithTags = null;
      MetricRegistryWithTags metricRegistryWithTags = null;

      if (metricRegistry instanceof MetricRegistryWithTags) {
        metricRegistryWithTags = (MetricRegistryWithTags) metricRegistry;
        countersWithTags = metricRegistryWithTags.getCountersWithTags();
        gaugesWithTags = metricRegistryWithTags.getGaugesWithTags();
        histogramsWithTags = metricRegistryWithTags.getHistogramsWithTags();
        metersWithTags = metricRegistryWithTags.getMetersWithTags();
        timersWithTags = metricRegistryWithTags.getTimersWithTags();
      }

      final long timestamp = clock.getTime() / 1000;

      try {
          graphite.connect();

          for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
              reportGauge(entry.getKey(), entry.getValue(), timestamp);
          }

          for (Map.Entry<String, Counter> entry : counters.entrySet()) {
              reportCounter(entry.getKey(), entry.getValue(), timestamp);
          }

          for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
              reportHistogram(entry.getKey(), entry.getValue(), timestamp);
          }

          for (Map.Entry<String, Meter> entry : meters.entrySet()) {
              reportMetered(entry.getKey(), entry.getValue(), timestamp);
          }

          for (Map.Entry<String, Timer> entry : timers.entrySet()) {
              reportTimer(entry.getKey(), entry.getValue(), timestamp);
          }

          if (metricRegistryWithTags != null) {
            for (CounterWithTags counterWithTags : countersWithTags) {
              reportCounter(counterWithTags.getMetricName(), counterWithTags, timestamp);
            }
            for (GaugeWithTags<?> gaugeWithTags : gaugesWithTags) {
              reportGauge(gaugeWithTags.getMetricName(), gaugeWithTags, timestamp);
            }
            for (HistogramWithTags histogramWithTags : histogramsWithTags) {
              reportHistogram(histogramWithTags.getMetricName(), histogramWithTags, timestamp);
            }
            for (MeterWithTags meterWithTags : metersWithTags) {
              reportMetered(meterWithTags.getMetricName(), meterWithTags, timestamp);
            }
            for (TimerWithTags timerWithTags : timersWithTags) {
              reportTimer(timerWithTags.getMetricName(), timerWithTags, timestamp);
            }
          }

          graphite.flush();
      } catch (IOException e) {
          LOGGER.warn("Unable to report to Graphite", graphite, e);
      } finally {
          try {
              graphite.close();
          } catch (IOException e1) {
              LOGGER.warn("Error closing Graphite", graphite, e1);
          }
      }
  }

  @Override
  public void stop() {
      try {
          super.stop();
      } finally {
          try {
              graphite.close();
          } catch (IOException e) {
              LOGGER.debug("Error disconnecting from Graphite", graphite, e);
          }
      }
  }

  /**
   * Building the metric / series name with tags:<br/>
   *
   * <a href="http://graphite.readthedocs.io/en/latest/tags.html">
   *          http://graphite.readthedocs.io/en/latest/tags.html
   * </a>
   *
   * @param metric
   * @return
   */
  private String getUnifiedTags(MetricRegistryWithTags metricRegistryWithTags, WithTags metric) {
    StringBuilder tags = new StringBuilder();
    Map<String, String> unifiedTags = new TreeMap<>();
    unifiedTags.putAll(metricRegistryWithTags.getTags());
    unifiedTags.putAll(metric.getTags());

    unifiedTags.forEach((tag, value) -> {
      tags.append(';');
      tags.append(tag.replaceAll("\\.", "_"));
      tags.append('=');
      if (value != null) {
        tags.append(value.replaceAll("\\.", "_"));
      }
    });
    return tags.toString();
  }

  private String getMetricRegistryTags(MetricRegistryWithTags metricRegistryWithTags) {
    StringBuilder tags = new StringBuilder();
    Map<String, String> unifiedTags = new TreeMap<>();
    unifiedTags.putAll(metricRegistryWithTags.getTags());

    unifiedTags.forEach((tag, value) -> {
      tags.append(';');
      tags.append(tag.replaceAll("\\.", "_"));
      tags.append('=');
      if (value != null) {
        tags.append(value.replaceAll("\\.", "_"));
      }
    });
    return tags.toString();
  }

  private String getTags(Metric metric) {
    String tags = "";
    if (metricRegistry instanceof MetricRegistryWithTags) {
      if (metric instanceof WithTags) {
        tags = getUnifiedTags((MetricRegistryWithTags) metricRegistry, (WithTags) metric);
      } else {
        tags = getMetricRegistryTags((MetricRegistryWithTags) metricRegistry);
      }
    }
    return tags;
  }

  private void reportTimer(String name, Timer timer, long timestamp) throws IOException {
      final Snapshot snapshot = timer.getSnapshot();
      String tags = getTags(timer);

      sendIfEnabled(MAX, name, tags, convertDuration(snapshot.getMax()), timestamp);
      sendIfEnabled(MEAN, name, tags, convertDuration(snapshot.getMean()), timestamp);
      sendIfEnabled(MIN, name, tags, convertDuration(snapshot.getMin()), timestamp);
      sendIfEnabled(STDDEV, name, tags, convertDuration(snapshot.getStdDev()), timestamp);
      sendIfEnabled(P50, name, tags, convertDuration(snapshot.getMedian()), timestamp);
      sendIfEnabled(P75, name, tags, convertDuration(snapshot.get75thPercentile()), timestamp);
      sendIfEnabled(P95, name, tags, convertDuration(snapshot.get95thPercentile()), timestamp);
      sendIfEnabled(P98, name, tags, convertDuration(snapshot.get98thPercentile()), timestamp);
      sendIfEnabled(P99, name, tags, convertDuration(snapshot.get99thPercentile()), timestamp);
      sendIfEnabled(P999, name, tags, convertDuration(snapshot.get999thPercentile()), timestamp);
      reportMetered(name, timer, timestamp);
  }

  private void reportMetered(String name, Metered meter, long timestamp) throws IOException {
      String tags = getTags(meter);

      sendIfEnabled(COUNT, name, tags, meter.getCount(), timestamp);
      sendIfEnabled(M1_RATE, name, tags, convertRate(meter.getOneMinuteRate()), timestamp);
      sendIfEnabled(M5_RATE, name, tags, convertRate(meter.getFiveMinuteRate()), timestamp);
      sendIfEnabled(M15_RATE, name, tags, convertRate(meter.getFifteenMinuteRate()), timestamp);
      sendIfEnabled(MEAN_RATE, name, tags, convertRate(meter.getMeanRate()), timestamp);
  }

  private void reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
      final Snapshot snapshot = histogram.getSnapshot();
      String tags = getTags(histogram);

      sendIfEnabled(COUNT, name, tags, histogram.getCount(), timestamp);
      sendIfEnabled(MAX, name, tags, snapshot.getMax(), timestamp);
      sendIfEnabled(MEAN, name, tags, snapshot.getMean(), timestamp);
      sendIfEnabled(MIN, name, tags, snapshot.getMin(), timestamp);
      sendIfEnabled(STDDEV, name, tags, snapshot.getStdDev(), timestamp);
      sendIfEnabled(P50, name, tags, snapshot.getMedian(), timestamp);
      sendIfEnabled(P75, name, tags, snapshot.get75thPercentile(), timestamp);
      sendIfEnabled(P95, name, tags, snapshot.get95thPercentile(), timestamp);
      sendIfEnabled(P98, name, tags, snapshot.get98thPercentile(), timestamp);
      sendIfEnabled(P99, name, tags, snapshot.get99thPercentile(), timestamp);
      sendIfEnabled(P999, name, tags, snapshot.get999thPercentile(), timestamp);
  }

  private void sendIfEnabled(MetricAttribute type, String name, String tags, double value, long timestamp) throws IOException {
      if (getDisabledMetricAttributes().contains(type)){
          return;
      }
      graphite.send(prefix(name, type.getCode()) + tags, format(value), timestamp);
  }

  private void sendIfEnabled(MetricAttribute type, String name, String tags, long value, long timestamp) throws IOException {
      if (getDisabledMetricAttributes().contains(type)){
          return;
      }
      graphite.send(prefix(name, type.getCode()) + tags, format(value), timestamp);
  }

  private void reportCounter(String name, Counter counter, long timestamp) throws IOException {
      String tags = getTags(counter);
      graphite.send(prefix(name, COUNT.getCode()) + tags, format(counter.getCount()), timestamp);
  }

  private void reportGauge(String name, @SuppressWarnings("rawtypes") Gauge gauge, long timestamp) throws IOException {
      final String value = format(gauge.getValue());
      if (value != null) {
          String tags = getTags(gauge);
          graphite.send(prefix(name) + tags, value, timestamp);
      }
  }

  private String format(Object o) {
      if (o instanceof Float) {
          return format(((Float) o).doubleValue());
      } else if (o instanceof Double) {
          return format(((Double) o).doubleValue());
      } else if (o instanceof Byte) {
          return format(((Byte) o).longValue());
      } else if (o instanceof Short) {
          return format(((Short) o).longValue());
      } else if (o instanceof Integer) {
          return format(((Integer) o).longValue());
      } else if (o instanceof Long) {
          return format(((Long) o).longValue());
      } else if (o instanceof BigInteger) {
          return format(((BigInteger) o).doubleValue());
      } else if (o instanceof BigDecimal) {
          return format(((BigDecimal) o).doubleValue());
      } else if (o instanceof Boolean) {
          return format(((Boolean) o) ? 1 : 0);
      }
      return null;
  }

  private String prefix(String... components) {
      return MetricRegistry.name(prefix, components);
  }

  private String format(long n) {
      return Long.toString(n);
  }

  protected String format(double v) {
      // the Carbon plaintext format is pretty underspecified, but it seems like it just wants
      // US-formatted digits
      return String.format(Locale.US, "%2.2f", v);
  }

}
