package controller.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;

@Slf4j
@Component
public class SwitchSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        SwitchSensorProto switchSensor = event.getSwitchSensor();

        log.info("Switch event: sensorId={}, state={}", event.getId(), switchSensor.getState());
    }
}
