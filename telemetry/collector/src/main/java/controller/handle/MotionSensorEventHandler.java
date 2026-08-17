package controller.handle;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class MotionSensorEventHandler extends BaseHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    protected void process(SensorEventProto event) {
        MotionSensorProto motionSensor = event.getMotionSensor();

        System.out.println(
                "Motion event: sensorId=" + event.getId()
                        + ", motion=" + motionSensor.getMotion()
                        + ", linkQuality=" + motionSensor.getLinkQuality()
                        + ", voltage=" + motionSensor.getVoltage()
        );
    }
}
