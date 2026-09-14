package controller.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;

@Slf4j
@Component
public class TemperatureSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        TemperatureSensorProto temperatureSensor = event.getTemperatureSensor();

        log.info("Temperature event: sensorId={}, temperatureC={}, temperatureF={}",
                event.getId(), temperatureSensor.getTemperatureC(), temperatureSensor.getTemperatureF());
    }
}