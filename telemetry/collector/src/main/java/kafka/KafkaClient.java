package kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaClient {

    Producer<String, SpecificRecordBase> getProducer();
    //Producer<String, byte[]> getProducer();

    void stop();
}
