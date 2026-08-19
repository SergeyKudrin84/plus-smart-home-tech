package analyzer.service;

import analyzer.model.ConditionType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class LightValueExtractor implements SensorValueExtractor {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.LUMINOSITY;
    }

    @Override
    public int extract(SensorStateAvro state, ConditionType type) {
        if (!supports(type)) {
            throw new IllegalArgumentException(
                    "Unsupported condition type: " + type
            );
        }

        LightSensorAvro sensor = (LightSensorAvro) state.getData();

        return sensor.getLuminosity();
    }
}