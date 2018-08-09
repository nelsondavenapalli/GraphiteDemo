package fun.abm.GraphiteDemo.metrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Meter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class MeterWithTags extends Meter implements WithTagsAndMetricName<MeterWithTags> {

  @Getter
  private final Map<String, String> tags;
  @Getter
  private final String metricName;

  public MeterWithTags(String metricName, Map<String, String> tags) {
    super();
    this.metricName = metricName;
    this.tags = Collections.unmodifiableMap(tags != null ? tags : new HashMap<>());
  }

  public MeterWithTags(String metricName, Map<String, String> tags, Clock clock) {
    super(clock);
    this.metricName = metricName;
    this.tags = Collections.unmodifiableMap(tags != null ? tags : new HashMap<>());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((metricName == null) ? 0 : metricName.hashCode());
    result = prime * result + ((tags == null) ? 0 : tags.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return CompareUtil.equals(this, obj);
  }

  @Override
  public int compareTo(MeterWithTags o) {
    int cmp = metricName.compareTo(o.metricName);
    if (cmp == 0) {
      cmp = Integer.compare(tags.hashCode(), o.tags.hashCode());
    }
    return cmp;
  }

}
