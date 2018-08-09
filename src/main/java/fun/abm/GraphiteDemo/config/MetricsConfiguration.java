package fun.abm.GraphiteDemo.config;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import fun.abm.GraphiteDemo.metrics.ApplicationMetricsManager;
import fun.abm.GraphiteDemo.metrics.GraphiteReporterWithTagSupport;
import fun.abm.GraphiteDemo.metrics.MetricRegistryWithTags;
import fun.abm.GraphiteDemo.metrics.PageMetricsCollector;
import fun.abm.GraphiteDemo.metrics.PrefixedMetricSet;
import fun.abm.GraphiteDemo.metrics.RequestMetricInterceptor;
import fun.abm.GraphiteDemo.metrics.SystemMetricsSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
@Import( GraphiteUpstreamConfiguration.class)
@Slf4j
public class MetricsConfiguration {

//
//  @Configuration
//  public static class WebMvcConfig extends WebMvcConfigurerAdapter {
//
//    @Autowired
//    RequestMetricInterceptor metricInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//      registry.addInterceptor(metricInterceptor);
//    }
//  }

  private static final String NAME = "metrics";
  private static final String[] NAMES = new String[]{"components-api"};
  private static final String NAMESPACE = "growth.components-api";

  @Autowired
  private Environment env;

  private MetricRegistryWithTags componentApiMetrics;

  @Bean
  ApplicationMetricsManager backendMetricsManager() {
    return new ApplicationMetricsManager();
  }

//  @Bean
//  public RequestMetricInterceptor requestMetricInterceptor() {
//    return new RequestMetricInterceptor();
//  }

  @Bean
  @Primary
  public MetricRegistry metricRegistry(MetricRegistryWithTags metricRegistry) {
    return metricRegistry;
  }

  @Bean
  static GraphiteUpstreamConfiguration graphiteUpstreamConfiguration(
    @Value("${graphite.host:#{null}}") String graphiteHost,
    @Value("${graphite.port:2003}") Integer graphitePort,
    @Value("${hostname:#{null}}") String hostname,
    @Value("${environment.name:#{null}}") String environmentName,
    @Value("${datacenter.name:#{null}}") String datacenter) {
    return new GraphiteUpstreamConfiguration(graphiteHost, graphitePort, hostname, environmentName, datacenter);
  }

  @Bean
  public MetricRegistryWithTags componentsApiMetricRegistry(GraphiteUpstreamConfiguration configuration) {
    componentApiMetrics = new MetricRegistryWithTags(NAMESPACE);
    componentApiMetrics.getTags().put("datacenter", configuration.getDatacenter());
    componentApiMetrics.getTags().put("env", configuration.getEnvironmentName());
    componentApiMetrics.getTags().put("hostname", configuration.getHostname());
    componentApiMetrics.getTags().put("git_commit", System.getenv("GIT_COMMIT"));
    componentApiMetrics.getTags().put("profiles", Arrays.toString(env.getActiveProfiles()));


    componentApiMetrics.registerAll(new PrefixedMetricSet("system", new SystemMetricsSet()));
    componentApiMetrics.registerAll(new PrefixedMetricSet("classes", new ClassLoadingGaugeSet()));
    componentApiMetrics.registerAll(new PrefixedMetricSet("gc", new GarbageCollectorMetricSet()));
    componentApiMetrics.registerAll(new PrefixedMetricSet("jvm", new JvmAttributeGaugeSet()));
    componentApiMetrics.registerAll(new PrefixedMetricSet("memory", new MemoryUsageGaugeSet()));
    componentApiMetrics.registerAll(new PrefixedMetricSet("thread", new ThreadStatesGaugeSet()));

    return componentApiMetrics;
  }

  @Bean
  public GraphiteSender graphiteSender(GraphiteUpstreamConfiguration configuration) {
    try {
      return configuration.createGraphite();
    } catch(IllegalStateException e) {
      log.warn("using FallbackGraphiteSender (noop) for metric data");
      return new FallbackGraphiteSender();
    }
  }

  @Bean(destroyMethod = "close")
  @Lazy(false)
  public Reporter componentsApiGraphiteMetricReporter(
      MetricRegistryWithTags metricRegistry,
      GraphiteSender graphiteSender) {

    ScheduledReporter reporter = GraphiteReporterWithTagSupport.forRegistry(metricRegistry)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build(graphiteSender);

    try {
      return reporter;
    } finally {
      reporter.start(1, TimeUnit.SECONDS);
    }
  }

  @Bean(destroyMethod = "close", initMethod = "start")
  @Lazy(false)
  public Reporter componentsApiJMXMetricReporter(
      MetricRegistryWithTags metricRegistry) {
    return JmxReporter.forRegistry(metricRegistry)
        .inDomain(MetricRegistry.name(NAME, NAMES))
        .build();
  }

  @Bean
  PageMetricsCollector metricsCollector(ApplicationMetricsManager metricsManager) {
    return new PageMetricsCollector(metricsManager);
  }


  @ManagedOperation
  public void reset() {
    componentApiMetrics.removeMatching(MetricFilter.ALL);
  }

  @Slf4j
  private static final class FallbackGraphiteSender implements GraphiteSender {

    @Override
    public void close() throws IOException {
    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
      log.debug("name={}, value={}, timestamp={}", name, value, timestamp);
    }

    @Override
    public boolean isConnected() {
      return true;
    }

    @Override
    public int getFailures() {
      return 0;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
    }
  }

}
