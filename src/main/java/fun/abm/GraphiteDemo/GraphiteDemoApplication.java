package fun.abm.GraphiteDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"fun.abm.GraphiteDemo"})
public class GraphiteDemoApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(GraphiteDemoApplication.class, args);
	}
}
