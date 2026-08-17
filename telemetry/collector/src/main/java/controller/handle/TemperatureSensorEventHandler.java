package controller.handle;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;

@Component
public class TemperatureSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        TemperatureSensorProto temperatureSensor = event.getTemperatureSensor();

        System.out.println(
                "Temperature event: sensorId=" + event.getId()
                        + ", temperatureC=" + temperatureSensor.getTemperatureC()
                        + ", temperatureF=" + temperatureSensor.getTemperatureF()
        );
    }
}