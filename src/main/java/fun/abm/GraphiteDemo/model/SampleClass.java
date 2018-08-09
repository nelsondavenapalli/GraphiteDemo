package fun.abm.GraphiteDemo.model;

import fun.abm.GraphiteDemo.metrics.MetricContext;
import fun.abm.GraphiteDemo.metrics.MetricRegistryWithTags;
import fun.abm.GraphiteDemo.metrics.PageMetricsCollector;
import fun.abm.GraphiteDemo.metrics.TimerWithTags;
import java.util.HashMap;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Data
@Component
@Slf4j
public class SampleClass {

  private  MetricRegistryWithTags metricRegistryWithTags;
  private PageMetricsCollector pageMetricsCollector;


  @Autowired
  public SampleClass(MetricRegistryWithTags metricRegistry, PageMetricsCollector  collector) {
    this.metricRegistryWithTags = metricRegistry;
    this.pageMetricsCollector = collector;
  }

  public void test1() throws InterruptedException {
    HashMap<String, String> tags = new HashMap<>();
    tags.put("abc", "a");
    tags.put("b", "b");

    PageMetricsCollector.Context collector = this.pageMetricsCollector.start();

    log.info("collecting metrics ");
    tags.put("methodName", "test1");
    metricRegistryWithTags.meterWithTags("methodName ", tags).mark();

    MetricContext getBySemanticKeyMetrics = MetricContext.methodCall(metricRegistryWithTags, "test1", tags);
    TimerWithTags.Context timerCtx = getBySemanticKeyMetrics.start();

    Thread.sleep(1000);
    timerCtx.close();
    getBySemanticKeyMetrics.incrementSuccess();

    collector.stopTimer();

  }

}
