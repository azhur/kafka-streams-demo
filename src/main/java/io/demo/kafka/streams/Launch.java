package io.demo.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;

import java.time.Duration;
import java.util.Properties;

public class Launch {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-demo");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        // within docker container
        //props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:19092,kafka2:19093,kafka3:19094");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, DefaultProductionExceptionHandler.class);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000);
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> messages = builder.stream("Messages", Consumed.with(Serdes.String(), Serdes.String()));
        final KStream<String, String> transactions = builder.stream("Transactions", Consumed.with(Serdes.String(), Serdes.String()));

        messages
                .leftJoin(transactions,
                        (value1, value2) -> value1 + value2,
                        JoinWindows.of(Duration.ofMinutes(1)))
                .to("MessagesAndTransactions");

        final Topology topology = builder.build();

        // https://zz85.github.io/kafka-streams-viz/
        System.out.println(topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println("Starting streams...");
        streams.start();
        System.out.println("Streams started!");

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
