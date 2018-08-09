package fun.abm.GraphiteDemo.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Slf4j
@Component
public class RequestMetricInterceptor extends HandlerInterceptorAdapter {

  private static final String STARTED = RequestMetricInterceptor.class.getName() + ".started";

  @Autowired
  private MetricRegistryWithTags metricRegistry;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    request.setAttribute(STARTED, System.nanoTime());
    return super.preHandle(request, response, handler);
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
    super.postHandle(request, response, handler, modelAndView);

    try {
      Long started = (Long) request.getAttribute(STARTED);
      Map<String, String> tags = new HashMap<>();
      String matchingPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

      tags.put("dispatcher_type", request.getDispatcherType().name());
      tags.put("async_started", Boolean.toString(request.isAsyncStarted()));
      tags.put("method", request.getMethod());
      tags.put("status_code", Integer.toString(response.getStatus(), 10));
      if (matchingPath == null) {
        matchingPath = "-";
      }
      tags.put("path", matchingPath);

      TimerWithTags timer = metricRegistry.timerWithTags("request", tags);
      timer.update(System.nanoTime() - started, TimeUnit.NANOSECONDS);
    } catch(Throwable t) {
      // silence every possible error
      log.debug("error during request metric measurement", t);
    }
  }

}
