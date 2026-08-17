package controller.handle;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class ClimateSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        ClimateSensorProto climateSensor = event.getClimateSensor();

        System.out.println(
                "Climate event: sensorId=" + event.getId()
                        + ", temperatureC=" + climateSensor.getTemperatureC()
                        + ", humidity=" + climateSensor.getHumidity()
                        + ", co2Level=" + climateSensor.getCo2Level()
        );
    }
}
