package analyzer.grpc;


import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import java.time.Instant;

@Component
public class HubRouterClient {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub client;

    public HubRouterClient(
            @GrpcClient("hub-router")
            HubRouterControllerGrpc.HubRouterControllerBlockingStub client
    ) {
        this.client = client;
    }

    public void sendAction(
            String hubId,
            String scenarioName,
            String sensorId,
            String actionType,
            Integer value,
            Instant timestamp
    ) {
        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(actionType));

        if (value != null) {
            actionBuilder.setValue(value);
        }

        DeviceActionRequestProto request = DeviceActionRequestProto.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(actionBuilder.build())
                //.setTimestamp(Timestamps.fromMillis(timestamp.toEpochMilli()))
                .build();

        client.handleDeviceAction(request);
    }
}
