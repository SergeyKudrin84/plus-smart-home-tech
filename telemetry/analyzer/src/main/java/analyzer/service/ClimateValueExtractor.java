package analyzer.service;

import analyzer.model.ConditionType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class ClimateValueExtractor implements SensorValueExtractor {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.HUMIDITY
                || type == ConditionType.CO2LEVEL;
    }

    @Override
    public int extract(SensorStateAvro state, ConditionType type) {
        if (!supports(type)) {
            throw new IllegalArgumentException(
                    "Unsupported condition type: " + type
            );
        }

        ClimateSensorAvro sensor = (ClimateSensorAvro) state.getData();

        return switch (type) {
            case HUMIDITY -> sensor.getHumidity();
            case CO2LEVEL -> sensor.getCo2Level();
            default -> throw new IllegalArgumentException(
                    "Unsupported climate condition type: " + type
            );
        };
    }
}
