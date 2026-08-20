package analyzer.kafka;

import analyzer.service.HubEventService;
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
    private final HubEventService hubEventService;

    public HubEventProcessor(@Qualifier("hubEventConsumerProperties") Properties properties,
                             HubEventService hubEventService) {
        this.consumer = new KafkaConsumer<>(properties);
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(HUBS));

        log.info("HubEventProcessor started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                var records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    hubEventService.process(record.value());
                }
            }
        } finally {
            consumer.close();
            log.info("HubEventProcessor stopped");
        }
    }
}