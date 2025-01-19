package org.flinkerManager.jobs;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flinkerManager.models.BenchmarkMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class KafkaDataStreamJob {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaDataStreamJob.class);

    public static void main(String[] args) throws Exception {

        String brokers = "192.168.49.2:30000";
        String inputTopic = "flink_input";
        String outputTopic = "flink_output";
        String groupId = "flink-group-id";

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 2);
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofMinutes(1));

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);


        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(brokers)
                .setTopics(inputTopic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> stream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5)),
                "Kafka Source");


        DataStream<BenchmarkMessage> parsedStream = stream.map(value -> {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                LOG.info("Kafka Message");
                BenchmarkMessage message = objectMapper.readValue(value, BenchmarkMessage.class);
                LOG.info("Parsed Message: " + message);
                return message;
            } catch (JsonParseException e) {
                LOG.error("JSON parsing error for message: {}", value, e);
                return null;
            } catch (Exception e) {
                LOG.error("Unexpected error for message: {}", value, e);
                return null;
            }
        }).filter(Objects::nonNull);

        DataStream<String> outputStream = parsedStream.map(message -> {
            LOG.info("parsedMessage {}", message);
            message.setMessage(message.getMessage());
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(message);
        });

        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setTopic(outputTopic)
                .setValueSerializationSchema(new SimpleStringSchema())
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(serializer)
                .build();

        outputStream.sinkTo(kafkaSink).name("Kafka Sink");

        env.execute("Kafka-to-Kafka Streaming Example");
    }
}
