package eu.profinit.manta.connector.java.analysis.kafka.target;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.apache.kafka.clients.CommonClientConfigs;

public class AbstractKafkaTarget {
    protected static final String PRODUCER_TOPIC = "ProducerTopic";
    protected static final String CONSUMER_TOPIC = "ConsumerTopic";

    @Nonnull
    protected static Properties getKafkaProperties() {
        return new Properties() {{
            setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "ServerName");
        }};
    }

    @Nonnull
    protected static Properties getKafkaPropertiesFromFile() {
        try {
            return new Properties() {{
                load(new FileReader("config/AbstractKafkaTarget/kafka.properties"));
            }};
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load property file", e);
        }
    }
}
