package analyzer.service;

import analyzer.model.ConditionType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class TemperatureValueExtractor implements SensorValueExtractor {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.TEMPERATURE;
    }

    @Override
    public int extract(SensorStateAvro state, ConditionType type) {
        if (!supports(type)) {
            throw new IllegalArgumentException(
                    "Unsupported condition type: " + type
            );
        }

        Object data = state.getData();

        if (data instanceof TemperatureSensorAvro sensor) {
            return sensor.getTemperatureC();
        }

        if (data instanceof ClimateSensorAvro sensor) {
            return sensor.getTemperatureC();
        }

        throw new IllegalArgumentException(
                "Unsupported sensor payload for TEMPERATURE: "
                        + data.getClass().getSimpleName()
        );
    }
}