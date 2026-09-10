package service;

import kafka.KafkaTopics;
import kafka.SensorEventConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import serialization.GeneralAvroSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final Logger log = LoggerFactory.getLogger(AggregationStarter.class);

    private final SensorEventConsumer consumer;
    private final SnapshotAggregator aggregator;

    public void start() {

        Producer<String, SensorsSnapshotAvro> producer = createProducer();

        try {
            while (true) {

                ConsumerRecords<String, SensorEventAvro> records = consumer.poll();

                for (ConsumerRecord<String, SensorEventAvro> record : records) {

                    SensorEventAvro event = record.value();

                    log.info("Получено событие датчика: {}", event);

                    aggregator.updateState(event)
                            .ifPresent(snapshot -> sendSnapshot(producer, snapshot));
                }

                consumer.commit();
            }

        } catch (WakeupException ignored) {
            // штатное завершение consumer
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
            } finally {
                consumer.close();
                producer.close();
            }
        }
    }

    private Producer<String, SensorsSnapshotAvro> createProducer() {

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        return new KafkaProducer<>(properties);
    }

    private void sendSnapshot(Producer<String, SensorsSnapshotAvro> producer, SensorsSnapshotAvro snapshot) {
        try {
            producer.send(
                    new ProducerRecord<>(
                            KafkaTopics.SNAPSHOTS,
                            snapshot.getHubId(),
                            snapshot
                    )
            ).get();

            log.info("Snapshot хаба {} успешно записан в Kafka", snapshot.getHubId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Поток был прерван при отправке snapshot", e);

        } catch (ExecutionException e) {
            throw new RuntimeException("Ошибка отправки snapshot в Kafka", e);
        }
    }
}
