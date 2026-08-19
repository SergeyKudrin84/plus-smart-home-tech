package analyzer.service;

import analyzer.model.ConditionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

import java.util.List;

@Component
public class SensorValueExtractorRegistry {

    private final List<SensorValueExtractor> extractors;

    public SensorValueExtractorRegistry(
            List<SensorValueExtractor> extractors
    ) {
        this.extractors = extractors;
    }

    public int extract(
            ConditionType type,
            SensorStateAvro state
    ) {
        return extractors.stream()
                .filter(extractor -> extractor.supports(type))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No extractor for condition type: " + type
                        )
                )
                .extract(state, type);
    }
}