package controller.handle;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;

@Component
public class SwitchSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        SwitchSensorProto switchSensor = event.getSwitchSensor();

        System.out.println(
                "Switch event: sensorId=" + event.getId()
                        + ", state=" + switchSensor.getState()
        );
    }
}
