package analyzer.kafka;

import analyzer.model.Condition;
import analyzer.model.ConditionOperation;
import analyzer.model.ConditionType;
import analyzer.model.Scenario;
import analyzer.repository.ScenarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.*;


import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static kafka.KafkaTopics.SNAPSHOTS;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final ScenarioRepository scenarioRepository;

    public SnapshotProcessor(@Qualifier("snapshotConsumerProperties") Properties properties,
                             ScenarioRepository scenarioRepository) {

        this.consumer = new KafkaConsumer<>(properties);
        this.scenarioRepository = scenarioRepository;
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

        var scenarios = scenarioRepository.findByHubId(snapshot.getHubId());

        scenarios.stream()
                .filter(scenario -> matches(scenario, snapshot))
                .forEach(scenario ->
                        log.info(
                                "Scenario '{}' matches snapshot of hub {}",
                                scenario.getName(),
                                snapshot.getHubId()
                        )
                );
    }

    private boolean matches(Scenario scenario, SensorsSnapshotAvro snapshot) {
        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> {
                    String sensorId = entry.getKey();
                    Condition condition = entry.getValue();

                    SensorStateAvro sensorState = snapshot.getSensorsState().get(sensorId);

                    if (sensorState == null) {
                        return false;
                    }

                    Object actualValue = extractValue(sensorState, condition.getType());

                    return compare(
                            actualValue,
                            condition.getValue(),
                            condition.getOperation()
                    );
                });
    }

    private Object extractValue(
            SensorStateAvro sensorState,
            ConditionType type
    ) {
        return switch (sensorState.getData()) {
            case ClimateSensorAvro data -> switch (type) {
                case TEMPERATURE -> data.getTemperatureC();
                case HUMIDITY -> data.getHumidity();
                case CO2LEVEL -> data.getCo2Level();
                default -> throw new IllegalArgumentException(
                        "Condition " + type + " is not supported by ClimateSensorAvro"
                );
            };

            case LightSensorAvro data -> {
                if (type != ConditionType.LUMINOSITY) {
                    throw new IllegalArgumentException(
                            "Condition " + type + " is not supported by LightSensorAvro"
                    );
                }

                yield data.getLuminosity();
            }

            case MotionSensorAvro data -> {
                if (type != ConditionType.MOTION) {
                    throw new IllegalArgumentException(
                            "Condition " + type + " is not supported by MotionSensorAvro"
                    );
                }

                yield data.getMotion();
            }

            case SwitchSensorAvro data -> {
                if (type != ConditionType.SWITCH) {
                    throw new IllegalArgumentException(
                            "Condition " + type + " is not supported by SwitchSensorAvro"
                    );
                }

                yield data.getState();
            }

            case TemperatureSensorAvro data -> {
                if (type != ConditionType.TEMPERATURE) {
                    throw new IllegalArgumentException(
                            "Condition " + type + " is not supported by TemperatureSensorAvro"
                    );
                }

                yield data.getTemperatureC();
            }

            default -> throw new IllegalArgumentException(
                    "Unknown sensor data type: " + sensorState.getData().getClass()
            );
        };
    }

    private boolean compare(
            Object actualValue,
            Integer expectedValue,
            ConditionOperation operation
    ) {
        int actual = toInt(actualValue);

        return switch (operation) {
            case EQUALS -> actual == expectedValue;
            case GREATER_THAN -> actual > expectedValue;
            case LOWER_THAN -> actual < expectedValue;
        };
    }

    private int toInt(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }

        return ((Number) value).intValue();
    }
}
