package controller.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@Component
public class MotionSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        MotionSensorProto motionSensor = event.getMotionSensor();

        log.info("Motion event: sensorId={}, motion={}, linkQuality={}, voltage={}",
                event.getId(), motionSensor.getMotion(), motionSensor.getLinkQuality(), motionSensor.getVoltage());
    }
}
