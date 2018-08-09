package fun.abm.GraphiteDemo.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

public class SystemMetricsSet implements MetricSet {

  @Override
  public Map<String, Metric> getMetrics() {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    Runtime runtime = Runtime.getRuntime();
    Map<String, Metric> gauges = new HashMap<>();
    gauges.put("mem.free", (Gauge<Long>) runtime::freeMemory);
    gauges.put("mem.max", (Gauge<Long>) runtime::maxMemory);
    gauges.put("processors", (Gauge<Integer>) runtime::availableProcessors);
    gauges.put("mem", (Gauge<Long>) runtime::totalMemory);
    gauges.put("uptime", (Gauge<Long>) runtimeMXBean::getUptime);
    gauges.put("systemload.average", (Gauge<Double>) operatingSystemMXBean::getSystemLoadAverage);
    return gauges;
  }
}