package analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotProcessor {

    public void process(SensorsSnapshotAvro snapshot) {
        log.info(
                "Обработка snapshot: hubId={}, sensors={}",
                snapshot.getHubId(),
                snapshot.getSensorsState().size()
        );

        for (Map.Entry<String, SensorStateAvro> entry
                : snapshot.getSensorsState().entrySet()) {

            String sensorId = entry.getKey();
            SensorStateAvro sensorState = entry.getValue();

            log.info(
                    "Датчик: id={}, timestamp={}, data={}",
                    sensorId,
                    sensorState.getTimestamp(),
                    sensorState.getData()
            );
        }
    }
}
