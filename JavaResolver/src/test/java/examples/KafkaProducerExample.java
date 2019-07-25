package examples;

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaProducerExample {
    public void sendData(String data) {
        Properties configuration = new Properties();
        configuration.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "ServerName");
        try (Producer<String, String> producer = new KafkaProducer<>(configuration)) {
            producer.send(new ProducerRecord<>("Topic", data));
        }
    }
}
