import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import service.AggregationStarter;

@SpringBootApplication
@ComponentScan(value = {"service", "kafka"})
public class Aggregator {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(
                        Aggregator.class,
                        args
                );

        AggregationStarter starter = context.getBean(AggregationStarter.class);
        starter.start();
    }
}
