package controller;

import com.google.protobuf.Empty;
import controller.handle.SensorEventHandler;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import kafka.KafkaClient;
import kafka.KafkaTopics;
import mapper.EventMapper;
import mapper.GrpcEventMapper;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> handlers;
    private final KafkaClient kafkaClient;
    private final GrpcEventMapper grpcEventMapper;
    private final EventMapper eventMapper;

    public EventController(Set<SensorEventHandler> handlers,
                           KafkaClient kafkaClient,
                           GrpcEventMapper grpcEventMapper,
                           EventMapper eventMapper) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity())
                );
        this.kafkaClient = kafkaClient;
        this.grpcEventMapper = grpcEventMapper;
        this.eventMapper = eventMapper;
    }

    @Override
    public void collectSensorEvent(
            SensorEventProto request,
            StreamObserver<Empty> responseObserver) {

        try {
            System.out.println("Sensor event received: " + request);

            SensorEventHandler handler = handlers.get(request.getPayloadCase());

            if (handler == null) {
                throw new IllegalArgumentException(
                        "Не найден обработчик для события " + request.getPayloadCase()
                );
            }

            handler.handle(request);

//            kafkaClient.getProducer().send(
//                    new ProducerRecord<>(
//                            kafka.KafkaTopics.SENSORS,
//                            request.getId(),
//                            grpcEventMapper.toBytes(request)
//                    )
//            );
//
            kafkaClient.getProducer().send(
                    new ProducerRecord<>(
                            KafkaTopics.SENSORS,
                            request.getId(),
                            eventMapper.toAvro(request)
                    )
            );

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }

    }

    @Override
    public void collectHubEvent(
            HubEventProto request,
            StreamObserver<Empty> responseObserver) {

        try {
            System.out.println("Hub event received: " + request);

            kafkaClient.getProducer().send(
                    new ProducerRecord<>(
                            KafkaTopics.HUBS,
                            request.getHubId(),
                            eventMapper.toAvro(request)
                    )
            );

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }

    }
}
