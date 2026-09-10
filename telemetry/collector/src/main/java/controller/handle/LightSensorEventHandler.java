package controller.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class LightSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        LightSensorProto lightSensor = event.getLightSensor();

        log.info("Light event: sensorId={}, luminosity={}, linkQuality={}",
                event.getId(), lightSensor.getLuminosity(), lightSensor.getLinkQuality());
    }
}