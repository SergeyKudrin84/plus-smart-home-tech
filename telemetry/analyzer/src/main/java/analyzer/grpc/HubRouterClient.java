package analyzer.grpc;


import analyzer.model.ActionType;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import com.google.protobuf.util.Timestamps;

import java.time.Instant;

@Slf4j
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
            ActionType actionType,
            Integer value,
            Instant timestamp
    ) {
        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(actionType.name()));

        if (value != null) {
            actionBuilder.setValue(value);
        }

        DeviceActionRequestProto request = DeviceActionRequestProto.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(actionBuilder.build())
                .setTimestamp(Timestamps.fromMillis(timestamp.toEpochMilli()))
                .build();

        client.handleDeviceAction(request);

        log.info(
                "Device action successfully sent: hubId={}, scenario={}, sensorId={}",
                hubId,
                scenarioName,
                sensorId
        );
    }
}
