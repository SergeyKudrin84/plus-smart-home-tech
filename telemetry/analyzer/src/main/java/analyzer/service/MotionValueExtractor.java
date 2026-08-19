package analyzer.service;

import analyzer.model.ConditionType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

@Component
public class MotionValueExtractor implements SensorValueExtractor {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.MOTION;
    }

    @Override
    public int extract(SensorStateAvro state, ConditionType type) {
        if (!supports(type)) {
            throw new IllegalArgumentException(
                    "Unsupported condition type: " + type
            );
        }

        MotionSensorAvro sensor = (MotionSensorAvro) state.getData();

        return sensor.getMotion() ? 1 : 0;
    }
}