package fun.abm.GraphiteDemo.controller;

import fun.abm.GraphiteDemo.model.SampleClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

  @Autowired
  private SampleClass sampleClass;

  @GetMapping("/metrics")
  public void getMetrics() throws InterruptedException {
    sampleClass.test1();
  }

}
