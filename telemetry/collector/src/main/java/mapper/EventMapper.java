package mapper;

import org.springframework.stereotype.Component;
import model.sensor.*;
import model.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.grpc.telemetry.event.*;
import com.google.protobuf.Timestamp;


import java.time.Instant;

@Component
public class EventMapper {

    public SensorEventAvro toAvro(SensorEventProto event) {
        Object payload = switch (event.getPayloadCase()) {
            case MOTION_SENSOR -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(event.getMotionSensor().getLinkQuality())
                    .setMotion(event.getMotionSensor().getMotion())
                    .setVoltage(event.getMotionSensor().getVoltage())
                    .build();

            case TEMPERATURE_SENSOR -> TemperatureSensorAvro.newBuilder()
                    .setId(event.getId())
                    .setHubId(event.getHubId())
                    .setTimestamp(toInstant(event.getTimestamp()))
                    .setTemperatureC(event.getTemperatureSensor().getTemperatureC())
                    .setTemperatureF(event.getTemperatureSensor().getTemperatureF())
                    .build();

            case LIGHT_SENSOR -> LightSensorAvro.newBuilder()
                    .setLinkQuality(event.getLightSensor().getLinkQuality())
                    .setLuminosity(event.getLightSensor().getLuminosity())
                    .build();

            case CLIMATE_SENSOR -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(event.getClimateSensor().getTemperatureC())
                    .setHumidity(event.getClimateSensor().getHumidity())
                    .setCo2Level(event.getClimateSensor().getCo2Level())
                    .build();

            case SWITCH_SENSOR -> SwitchSensorAvro.newBuilder()
                    .setState(event.getSwitchSensor().getState())
                    .build();
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(toInstant(event.getTimestamp()))
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(HubEventProto event) {
        Object payload = switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto e = event.getDeviceAdded();

                yield DeviceAddedEventAvro.newBuilder()
                        .setId(e.getId())
                        .setType(DeviceTypeAvro.valueOf(e.getType().name()))
                        .build();
            }

            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto e = event.getDeviceRemoved();

                yield DeviceRemovedEventAvro.newBuilder()
                        .setId(e.getId())
                        .build();
            }

            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto e = event.getScenarioAdded();

                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(e.getName())
                        .setConditions(e.getConditionList().stream()
                                .map(this::toAvro)
                                .toList())
                        .setActions(e.getActionList().stream()
                                .map(this::toAvro)
                                .toList())
                        .build();
            }

            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto e = event.getScenarioRemoved();

                yield ScenarioRemovedEventAvro.newBuilder()
                        .setName(e.getName())
                        .build();
            }
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(toInstant(event.getTimestamp()))
                .setPayload(payload)
                .build();
    }

    private ScenarioConditionAvro toAvro(ScenarioConditionProto condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        if (condition.hasBoolValue()) {
            builder.setValue(condition.getBoolValue());
        } else if (condition.hasIntValue()) {
            builder.setValue(condition.getIntValue());
        }

        return builder.build();
    }

    private DeviceActionAvro toAvro(DeviceActionProto action) {
        DeviceActionAvro.Builder builder = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()));

        if (action.hasValue()) {
            builder.setValue(action.getValue());
        }

        return builder.build();
    }

    private Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos()
        );
    }

    private long toMillis(Timestamp timestamp) {
        return timestamp.getSeconds() * 1000
                + timestamp.getNanos() / 1_000_000;
    }
}
