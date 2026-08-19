package analyzer.service;

import analyzer.model.Scenario;
import analyzer.model.ScenarioCondition;
import analyzer.repository.ScenarioConditionRepository;
import analyzer.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ConditionEvaluator conditionEvaluator;
    private final SensorValueExtractorRegistry valueExtractorRegistry;

    public void process(SensorsSnapshotAvro snapshot) {

        String hubId = snapshot.getHubId();

        log.info(
                "Получен snapshot: hubId={}, timestamp={}",
                hubId,
                snapshot.getTimestamp()
        );

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        for (Scenario scenario : scenarios) {
            if (isScenarioTriggered(scenario, sensorsState)) {
                log.info(
                        "Сценарий '{}' активирован для hubId={}",
                        scenario.getName(),
                        hubId
                );
            }
        }
    }

    private boolean isScenarioTriggered(
            Scenario scenario,
            Map<String, SensorStateAvro> sensorsState
    ) {
        List<ScenarioCondition> conditions =
                scenarioConditionRepository.findByScenarioId(scenario.getId());

        return conditions.stream()
                .allMatch(condition ->
                        isConditionSatisfied(condition, sensorsState)
                );
    }

    private boolean isConditionSatisfied(
            ScenarioCondition scenarioCondition,
            Map<String, SensorStateAvro> sensorsState
    ) {
        String sensorId = scenarioCondition.getSensor().getId();

        SensorStateAvro sensorState = sensorsState.get(sensorId);

        if (sensorState == null) {
            return false;
        }

        var condition = scenarioCondition.getCondition();

        int actualValue = valueExtractorRegistry.extract(
                condition.getType(),
                sensorState
        );

        return conditionEvaluator.evaluate(
                actualValue,
                condition.getOperation(),
                condition.getValue()
        );
    }
}