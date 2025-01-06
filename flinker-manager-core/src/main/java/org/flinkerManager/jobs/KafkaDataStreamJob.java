package org.flinkerManager.jobs;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaDataStreamJob {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaDataStreamJob.class);

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String brokers = "192.168.49.2:30000";
        String inputTopic = "flink_input";
        String outputTopic = "flink_output";
        String groupId = "flink-group-id";

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(brokers)
                .setTopics(inputTopic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

//        DataStream<String> stream = env.fromSource(
//                kafkaSource,
//                WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5)),
//                "Kafka Source");

        DataStream<String> stream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Source");


        stream.map(value -> {
            LOG.info("Consumed: " + value);
            return value;
        });

        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setTopic(outputTopic)
                .setValueSerializationSchema(new SimpleStringSchema())
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(serializer)
                .build();

        stream.sinkTo(kafkaSink).name("Kafka Sink");

        env.execute("Kafka-to-Kafka Streaming Example");
    }
}
