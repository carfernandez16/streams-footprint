package org.streams.footprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class App1 {

    private static final Logger logger = LoggerFactory.getLogger(App1.class);

    private Set<String> topics;
    private KafkaStreams streams;

    public App1(){
        this.topics = new HashSet<>();
    }

    private void run() {
        logger.info("#################### Stream App 1 #################");
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        buildTopology(streamsBuilder);
        streams = new KafkaStreams(streamsBuilder.build(), getProperties());
        streams.setUncaughtExceptionHandler((t, e) -> {
            logger.error("Stream App 1 error");
        });

        // Now run the processing topology via `start()` to begin processing its input data.
        streams.start();
    }

    private void buildTopology(StreamsBuilder streamsBuilder) {
        registerInTopics();

        Serde<String> stringSerde = Serdes.String();
        KStream<String, String> message = streamsBuilder.stream(topics, Consumed.with(stringSerde, stringSerde));
        MessageTransformer1 transformer = new MessageTransformer1();
        KStream<String, String> newMessage = message.transform(transformer);
        newMessage.to("___topic___2");
    }

    private void registerInTopics() {
        topics.add("___topic___1");
    }

    private static Properties getProperties() {
        Properties settings = new Properties();
        // Set a few key parameters
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream_app_1");
        // Kafka bootstrap server (broker to talk to); ubuntu is the host name for my VM running Kafka, port 9092 is where the (single) broker listens
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        // default serdes for serializing and deserializing key and value from and to streams in case no specific Serde is specified
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.STATE_DIR_CONFIG, "/temp");

        return settings;
    }

    private Runnable stop() {
        logger.info("Stopping Stream App 1");
        return streams::close;
    }

    public static void main(String[] args){
        App1 app1 = new App1();
        app1.run();

        // Add shutdown hook to respond to SIGTERM and gracefully close the Streams application.
        Runtime.getRuntime().addShutdownHook(new Thread(app1.stop()));
    }

}
