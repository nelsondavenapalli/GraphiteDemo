package fun.abm.GraphiteDemo.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Timer;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.servlet.ModelAndView;

public class PageMetricsCollector {

  private final static String[] TIMER_KEYS = {"type", "custom_template"};

  static public class Prefix {

    public static final String PAGE_DTO = "page_dto";
  }

  private final ApplicationMetricsManager applicationMetricsManager;

  public PageMetricsCollector(ApplicationMetricsManager applicationMetricsManager) {
    this.applicationMetricsManager = applicationMetricsManager;
  }

  public Context start() {
    return new Context();
  }

  public class Context {

    final Timer.Context requestTimer;

    private Context() {
      requestTimer = applicationMetricsManager.getTimerStarted(ApplicationMetricsManager.REQUEST_TIMER);
    }

    public long stopTimer(ModelAndView page) {
      long processingTime = this.stopTimer();
      updateTimers(page, processingTime);
      return processingTime;
    }

    public long stopTimer() {
      return requestTimer.stop();
    }

    private void updateTimers(ModelAndView page, long processingTime) {
      List<String> timers = getPageTimers(page);
      applicationMetricsManager.updateTimers(processingTime, timers);
    }

    private List<String> getPageTimers(ModelAndView page) {
      List<String> timers = new ArrayList<>();

      for (String timerName : TIMER_KEYS) {
        if (page.getModelMap().containsKey(timerName)) {
          String timer = page.getModelMap().get(timerName).toString();
          timers.add(name(Prefix.PAGE_DTO, timer));
        }
      }

      return timers;
    }
  }
}
