package analyzer.service;

import analyzer.model.ConditionType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Component
public class SwitchValueExtractor implements SensorValueExtractor {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.SWITCH;
    }

    @Override
    public int extract(SensorStateAvro state, ConditionType type) {
        if (!supports(type)) {
            throw new IllegalArgumentException(
                    "Unsupported condition type: " + type
            );
        }

        SwitchSensorAvro sensor = (SwitchSensorAvro) state.getData();

        return sensor.getState() ? 1 : 0;
    }
}