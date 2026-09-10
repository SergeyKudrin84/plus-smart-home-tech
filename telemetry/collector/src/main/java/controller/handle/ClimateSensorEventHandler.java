package controller.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class ClimateSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        ClimateSensorProto climateSensor = event.getClimateSensor();

        log.info("Climate event: sensorId={}, temperatureC={}, humidity={}, co2Level={}",
                event.getId(), climateSensor.getTemperatureC(), climateSensor.getHumidity(), climateSensor.getCo2Level());
    }
}
