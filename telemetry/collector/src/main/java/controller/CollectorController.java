package controller;

import jakarta.validation.Valid;
import kafka.KafkaClient;
import kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import mapper.EventMapper;
import model.hub.HubEvent;
import model.sensor.SensorEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final EventMapper eventMapper;
    private final KafkaClient kafkaClient;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        SensorEventAvro avroEvent = eventMapper.toAvro(event);
        System.out.println("Sensor event: " + event);
//        kafkaClient.getProducer().send(
//                new ProducerRecord<>(
//                        KafkaTopics.SENSORS,
//                        event.getId(),
//                        avroEvent
//                )
//        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        HubEventAvro avroEvent = eventMapper.toAvro(event);
//        kafkaClient.getProducer().send(
//                new ProducerRecord<>(
//                        KafkaTopics.HUBS,
//                        event.getHubId(),
//                        avroEvent
//                )
//        );
        System.out.println("Hub event: " + event);
        return ResponseEntity.ok().build();
    }
}
