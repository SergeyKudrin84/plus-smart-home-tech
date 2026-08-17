package mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class GrpcEventMapper {

    public byte[] toBytes(SensorEventProto event) {
        return event.toByteArray();
    }

}
