package analyzer.service;

import analyzer.model.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

public interface SensorValueExtractor {

    boolean supports(ConditionType type);

    int extract(SensorStateAvro state, ConditionType type);
}