package kafka;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import serialization.SensorEventDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
public class SensorEventConsumer {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    public SensorEventConsumer() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator-sensors");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);

        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(List.of(KafkaTopics.SENSORS));
    }

    public ConsumerRecords<String, SensorEventAvro> poll() {
        return consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
    }

    public void commit() {
        consumer.commitSync();
    }

    public void close() {
        consumer.close();
    }
}
