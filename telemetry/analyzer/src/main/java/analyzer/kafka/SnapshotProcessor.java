package analyzer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static kafka.KafkaTopics.SNAPSHOTS;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    public SnapshotProcessor(@Qualifier("snapshotConsumerProperties") Properties properties) {
        this.consumer = new KafkaConsumer<>(properties);
    }

    public void start() {
        consumer.subscribe(Collections.singletonList(SNAPSHOTS));

        log.info("SnapshotProcessor started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                var records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    process(record.value());
                }
            }
        } finally {
            consumer.close();
            log.info("SnapshotProcessor stopped");
        }
    }

    private void process(SensorsSnapshotAvro snapshot) {
        log.info(
                "Received snapshot: hubId={}",
                snapshot.getHubId()
        );
    }
}
