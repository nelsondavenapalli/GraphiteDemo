package fun.abm.GraphiteDemo.metrics;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;

public class ApplicationMetricsManager {

  public static final String CHECK = "DEP CHECK ";

  public static final String REQUEST_TIMER = "requestTimer";

  @Value("${graphite.host:#{null}}")
  private String graphiteHost;

  @Value("${graphite.port:2003}")
  private Integer graphitePort;

  @Value("${hostname:#{null}}")
  private String hostname;

  @Value("${environment.name:#{null}}")
  private String environmentName;

  @Value("${datacenter.name:#{null}}")
  private String datacenter;

  @Autowired
  private MetricRegistry metricRegistry;

  public Counter getCounter(String name) {
    return metricRegistry.counter(name);
  }

  public void countMetrics(String counter) {
    metricRegistry.counter(counter).inc();
  }

  public Meter getMeter(String name) {
    return metricRegistry.meter(name);
  }

  public long updateTimers(long elapsedTime, List<String> names) {

    for (String name : names) {
      Timer timer = metricRegistry.timer(name);
      timer.update(elapsedTime, NANOSECONDS);
    }

    return elapsedTime;
  }

  public Timer.Context getTimerStarted(String name) {
    return metricRegistry.timer(name).time();
  }

  public Timer getTimer(String name) {
    return metricRegistry.timer(name);
  }

  @ManagedOperation
  public void reset() {
    metricRegistry.removeMatching(MetricFilter.ALL);
  }
}
