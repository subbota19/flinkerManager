package org.flinkerManager.jobs;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;
import org.flinkerManager.mappers.BenchmarkMessageRowDataMapper;
import org.flinkerManager.models.BenchmarkMessage;
import org.flinkerManager.sink.BenchmarkMessageIcebergSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class KafkaIcebergDataStreamJob {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaIcebergDataStreamJob.class);

    public static void main(String[] args) throws Exception {

        String brokers = "192.168.49.2:30000";
        String inputTopic = "flink_input";
        String groupId = "flink-group-id";

        String catalogImpl = "org.apache.iceberg.jdbc.JdbcCatalog";
        String catalogName = "iceberg_catalog";
        String rawZoneDatabase = "raw_zone";
        String benchmarkMessageTable = "benchmark_message";

        Configuration flinkConf = new Configuration();
        flinkConf.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        flinkConf.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 2);
        flinkConf.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofMinutes(1));

        org.apache.hadoop.conf.Configuration hadoopConf = new org.apache.hadoop.conf.Configuration(false);

        Map<String, String> catalogProperties = Map.ofEntries(
                Map.entry("type", "iceberg"),
                Map.entry("catalog-impl", catalogImpl),
                Map.entry("io-impl", "org.apache.iceberg.aws.s3.S3FileIO"),
                Map.entry("uri", "jdbc:postgresql://postgres:5432/postgres"),
                Map.entry("jdbc.driver", "org.postgresql.Driver"),
                Map.entry("jdbc.user", "postgres"),
                Map.entry("jdbc.password", "postgres"),
                Map.entry("warehouse", "s3://datalake"),
                Map.entry("s3.endpoint", "http://minio:9000"),
                Map.entry("s3.access-key-id", "ADMIN"),
                Map.entry("s3.secret-access-key", "12345678"),
                Map.entry("s3.path-style-access", "true"),
                Map.entry("client.region", "us-east-1")
        );


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(flinkConf);

        env.enableCheckpointing(10000);

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
                "Kafka Source"
        );


        DataStream<BenchmarkMessage> parsedStream = stream.flatMap(new FlatMapFunction<String, BenchmarkMessage>() {

            @Override
            public void flatMap(String value, Collector<BenchmarkMessage> out) {

                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    BenchmarkMessage message = objectMapper.readValue(value, BenchmarkMessage.class);
                    LOG.info("Parsed Message: " + message);
                    if (!message.isEmpty()) {
                        out.collect(message);
                    }
                } catch (JsonParseException e) {
                    LOG.error("JSON parsing error for message: {}", value, e);
                } catch (Exception e) {
                    LOG.error("Unexpected error for message: {}", value, e);
                }
            }
        }).filter(Objects::nonNull);

        CatalogLoader catalogLoader = CatalogLoader.custom(
                catalogName,
                catalogProperties,
                hadoopConf,
                catalogImpl
        );

        TableIdentifier tableIdentifier = TableIdentifier.of(
                rawZoneDatabase,
                benchmarkMessageTable
        );

        TableLoader tableLoader = TableLoader.fromCatalog(
                catalogLoader,
                tableIdentifier
        );

        BenchmarkMessageRowDataMapper mapper = new BenchmarkMessageRowDataMapper();
        BenchmarkMessageIcebergSink benchmarkMessageIcebergSink = new BenchmarkMessageIcebergSink();

        benchmarkMessageIcebergSink.sinkToIceberg(
                parsedStream,
                tableLoader,
                mapper
        );

        env.execute("Kafka-to-Kafka Streaming Example");
    }


}
