package analyzer.service;

import analyzer.repository.ScenarioConditionRepository;
import analyzer.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ConditionEvaluator conditionEvaluator;
    private final SensorValueExtractorRegistry valueExtractorRegistry;

    public void process(SensorsSnapshotAvro snapshot) {
        log.info(
                "Получен snapshot: hubId={}, timestamp={}",
                snapshot.getHubId(),
                snapshot.getTimestamp()
        );

        // следующий шаг:
        // 1. разобрать состояния датчиков
        // 2. найти сценарии этого hub
        // 3. проверить conditions
        // 4. сформировать actions
        // 5. отправить команды в Hub Router
    }
}