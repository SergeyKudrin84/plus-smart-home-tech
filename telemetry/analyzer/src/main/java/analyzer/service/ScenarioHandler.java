package analyzer.service;

import analyzer.model.*;
import analyzer.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
@RequiredArgsConstructor
public class ScenarioHandler {

    private final ScenarioRepository scenarioRepository;

    public void add(HubEventAvro event) {
        ScenarioAddedEventAvro scenarioEvent = (ScenarioAddedEventAvro) event.getPayload();

        Scenario scenario = Scenario.builder()
                .hubId(event.getHubId())
                .name(scenarioEvent.getName())
                .build();

        scenarioEvent.getConditions().forEach(condition ->
                scenario.getConditions().put(
                        condition.getSensorId(),
                        toCondition(condition)
                )
        );

        scenarioEvent.getActions().forEach(action ->
                scenario.getActions().put(
                        action.getSensorId(),
                        toAction(action)
                )
        );

        scenarioRepository.save(scenario);
    }

    public void remove(HubEventAvro event) {
        ScenarioRemovedEventAvro scenarioEvent =
                (ScenarioRemovedEventAvro) event.getPayload();

        scenarioRepository.findByHubIdAndName(
                event.getHubId(),
                scenarioEvent.getName()
        ).ifPresent(scenarioRepository::delete);
    }

    private Condition toCondition(ScenarioConditionAvro condition) {
        return Condition.builder()
                .type(ConditionType.valueOf(condition.getType().name()))
                .operation(ConditionOperation.valueOf(condition.getOperation().name()))
                .value(extractConditionValue(condition))
                .build();
    }

    private Integer extractConditionValue(ScenarioConditionAvro condition) {
        Object value = condition.getValue();

        if (value == null) {
            return null;
        }

        if (value instanceof Integer integer) {
            return integer;
        }

        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }

        throw new IllegalArgumentException(
                "Unsupported condition value type: " + value.getClass()
        );
    }

    private Action toAction(DeviceActionAvro action) {
        return Action.builder()
                .type(ActionType.valueOf(action.getType().name()))
                .value(action.getValue())
                .build();
    }
}
