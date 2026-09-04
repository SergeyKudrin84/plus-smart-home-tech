package controller.handle;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public abstract class BaseHandler implements SensorEventHandler {

    @Override
    public void handle(SensorEventProto event) {
        if (event.getPayloadCase() != getMessageType()) {
            throw new IllegalArgumentException(
                    "Неверный тип события: " + event.getPayloadCase()
            );
        }

        process(event);
    }

    protected abstract void process(SensorEventProto event);
}