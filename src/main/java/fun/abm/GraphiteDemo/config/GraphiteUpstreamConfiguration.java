package fun.abm.GraphiteDemo.config;

import com.codahale.metrics.graphite.Graphite;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@RequiredArgsConstructor
@Component
public class GraphiteUpstreamConfiguration {

  private final String graphiteHost;
  private final Integer graphitePort;
  private final String hostname;
  private final String environmentName;
  private final String datacenter;

  private boolean enabled;

  @PostConstruct
  void checkConfig() {
    enabled = graphitePort != null && !StringUtils.isAnyBlank(graphiteHost, hostname, environmentName, datacenter);
    if (!enabled) {
      log.error("Graphite reporting disabled because missing configuration data."
          + "graphite.host = {}, graphite.port = {}, hostname = {}, environment.name = {}, datacenter.name = {}",
        graphiteHost, graphitePort, hostname, environmentName, datacenter);
      return;
    }
  }

  public Graphite createGraphite() {
    if (!enabled) {
      log.info("Can't create new graphite instance because of missing configuration");
      throw new IllegalStateException("Can't create new graphite instance because of missing configuration");
    }

    log.info("Created graphite instance with host {} and port {}", graphiteHost, graphitePort);
    return new Graphite(graphiteHost, graphitePort);
  }
}
