package analyzer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static kafka.KafkaTopics.HUBS;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> consumer;

    public HubEventProcessor(@Qualifier("hubEventConsumerProperties") Properties properties) {
        this.consumer = new KafkaConsumer<>(properties);
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(HUBS));

        log.info("HubEventProcessor started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                var records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    process(record.value());
                }
            }
        } finally {
            consumer.close();
            log.info("HubEventProcessor stopped");
        }
    }

    private void process(HubEventAvro event) {
        log.info(
                "Received hub event: hubId={}, payload={}",
                event.getHubId(),
                event.getPayload()
        );
    }
}