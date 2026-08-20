package analyzer.service;

import analyzer.model.Sensor;
import analyzer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioHandler scenarioHandler;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void process(HubEventAvro event) {
        switch (event.getPayload()) {
            case DeviceAddedEventAvro device -> sensorRepository.save(
                    Sensor.builder()
                            .id(device.getId())
                            .hubId(event.getHubId())
                            .build()
            );
            case DeviceRemovedEventAvro device -> sensorRepository.deleteById(device.getId());
            case ScenarioAddedEventAvro ignored -> scenarioHandler.add(event);
            case ScenarioRemovedEventAvro ignored -> scenarioHandler.remove(event);
            default -> log.info(
                    "Received hub event: hubId={}, payload={}",
                    event.getHubId(),
                    event.getPayload()
            );
        }
    }
}