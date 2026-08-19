package analyzer.kafka;

import analyzer.service.AnalyzerService;
import analyzer.service.SnapshotProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static kafka.KafkaTopics.SNAPSHOTS;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static kafka.KafkaTopics.SNAPSHOTS;

@Component
public class AnalyzerKafkaConsumer implements SmartLifecycle {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    //private final SnapshotProcessor snapshotProcessor;
    private final AnalyzerService analyzerService;

    private volatile boolean running;

    public AnalyzerKafkaConsumer(Properties kafkaConsumerProperties, AnalyzerService analyzerService) {
        this.consumer = new KafkaConsumer<>(kafkaConsumerProperties);
        this.analyzerService = analyzerService;
    }

    @Override
    public void start() {
        consumer.subscribe(Collections.singletonList(SNAPSHOTS));

        running = true;

        executor.submit(this::poll);
    }

    private void poll() {
        try {
            while (running) {
                var records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    SensorsSnapshotAvro snapshot = record.value();

                    analyzerService.process(snapshot);
                }
            }
        } finally {
            consumer.close();
        }
    }

    @Override
    public void stop() {
        running = false;
        executor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
