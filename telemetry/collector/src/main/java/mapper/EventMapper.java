package mapper;

import org.springframework.stereotype.Component;
import model.sensor.*;
import model.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@Component
public class EventMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        Object payload = switch (event) {
            case MotionSensorEvent e -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setMotion(e.isMotion())
                    .setVoltage(e.getVoltage())
                    .build();

            case TemperatureSensorEvent e -> TemperatureSensorAvro.newBuilder()
                    .setId(e.getId())
                    .setHubId(e.getHubId())
                    .setTimestamp(e.getTimestamp())
                    .setTemperatureC(e.getTemperatureC())
                    .setTemperatureF(e.getTemperatureF())
                    .build();

            case LightSensorEvent e -> LightSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setLuminosity(e.getLuminosity())
                    .build();

            case ClimateSensorEvent e -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setHumidity(e.getHumidity())
                    .setCo2Level(e.getCo2Level())
                    .build();

            case SwitchSensorEvent e -> SwitchSensorAvro.newBuilder()
                    .setState(e.isState())
                    .build();
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(HubEvent event) {
        Object payload = switch (event) {
            case DeviceAddedEvent e -> DeviceAddedEventAvro.newBuilder()
                    .setId(e.getId())
                    .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                    .build();

            case DeviceRemovedEvent e -> DeviceRemovedEventAvro.newBuilder()
                    .setId(e.getId())
                    .build();

            case ScenarioAddedEvent e -> ScenarioAddedEventAvro.newBuilder()
                    .setName(e.getName())
                    .setConditions(e.getConditions().stream()
                            .map(this::toAvro)
                            .toList())
                    .setActions(e.getActions().stream()
                            .map(this::toAvro)
                            .toList())
                    .build();

            case ScenarioRemovedEvent e -> ScenarioRemovedEventAvro.newBuilder()
                    .setName(e.getName())
                    .build();
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private ScenarioConditionAvro toAvro(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro toAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
