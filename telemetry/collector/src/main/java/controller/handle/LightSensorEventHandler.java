package controller.handle;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class LightSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        LightSensorProto lightSensor = event.getLightSensor();

        System.out.println(
                "Light event: sensorId=" + event.getId()
                        + ", luminosity=" + lightSensor.getLuminosity()
                        + ", linkQuality=" + lightSensor.getLinkQuality()
        );
    }
}