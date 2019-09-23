package examples;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerExample {
    public void readData() {
        Properties configuration = new Properties();
        configuration.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "ServerName");
        try (Consumer<String, String> consumer = new KafkaConsumer<>(configuration)) {
            consumer.subscribe(Collections.singletonList("Topic"));
            ConsumerRecords<String, String> data = consumer.poll(Duration.ofSeconds(1));
            // handle incoming data
        }
    }
}
