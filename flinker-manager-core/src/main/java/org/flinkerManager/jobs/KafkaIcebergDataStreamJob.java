package org.flinkerManager.jobs;

import com.amazonaws.services.dynamodbv2.xspec.L;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.*;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;
import org.flinkerManager.mappers.BenchmarkMessageRowDataMapper;
import org.flinkerManager.models.BenchmarkMessage;
import org.flinkerManager.sink.BenchmarkMessageIcebergSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public class KafkaIcebergDataStreamJob {
    private static final long CHECKPOINT_INTERVAL = 120_000L;
    private static final String RESTART_STRATEGY_TYPE = "fixed-delay";
    private static final String CHECKPOINT_STORAGE_TYPE = "filesystem";
    private static final String CHECKPOINTS_DIRECTORY_PATH = "s3://flink/checkpoints";
    private static final String STATE_BACKEND_TYPE = "rocksdb";
    private static final int RESTART_ATTEMPTS = 2;
    private static final Duration RESTART_DELAY = Duration.ofMinutes(1);
    private static final Duration WATERMARK_BOUND_DURATION = Duration.ofSeconds(5);

    private static final Duration WATERMARK_IDLENESS_DURATION = Duration.ofMinutes(1);
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";

    private static final Logger LOG = LoggerFactory.getLogger(KafkaIcebergDataStreamJob.class);

    public static void main(String[] args) throws Exception {


        ParameterTool params = ParameterTool.fromArgs(args);

        String brokers = params.get("kafka.brokers", "192.168.49.2:30000");
        String inputTopic = params.get("kafka.topic", "flink_input");
        String groupId = params.get("kafka.group.id", "flink-group-id");

        String catalogType = params.get("catalog.type", "iceberg");
        String catalogName = params.get("iceberg.catalog.name", "iceberg_catalog");
        String catalogImpl = params.get("iceberg.catalog.impl", "org.apache.iceberg.jdbc.JdbcCatalog");
        String warehousePath = params.get("iceberg.catalog.warehouse", "s3://datalake");
        String catalogIOImpl = params.get("iceberg.catalog.io-impl", "org.apache.iceberg.aws.s3.S3FileIO");

        String rawZoneDatabase = params.get("iceberg.database", "raw_zone");
        String benchmarkMessageTable = params.get("iceberg.table", "benchmark_message");

        String jdbcDriver = params.get("jdbc.driver", "org.postgresql.Driver");
        String jdbcUri = params.get("jdbc.uri", "jdbc:postgresql://postgres:5432/postgres");
        String jdbcUser = System.getenv("JDBC_USER");
        String jdbcPassword = System.getenv("JDBC_PASSWORD");

        Configuration flinkConf = new Configuration();

        flinkConf.set(RestartStrategyOptions.RESTART_STRATEGY, RESTART_STRATEGY_TYPE);
        flinkConf.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, RESTART_ATTEMPTS);
        flinkConf.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, RESTART_DELAY);
        flinkConf.set(CheckpointingOptions.CHECKPOINT_STORAGE, CHECKPOINT_STORAGE_TYPE);
        flinkConf.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, CHECKPOINTS_DIRECTORY_PATH);
        flinkConf.set(StateBackendOptions.STATE_BACKEND, STATE_BACKEND_TYPE);

        Configuration globalConf = GlobalConfiguration.loadConfiguration();

        String s3AccessKey = globalConf.getString("s3.access-key", null);
        String s3SecretKey = globalConf.getString("s3.secret-key", null);
        String s3Endpoint = globalConf.getString("s3.endpoint", null);
        String s3PathStyle = globalConf.getString("s3.path.style.access", "true");
        String s3ClientRegion = globalConf.getString("s3.client.region", "us-east-1");

        Map<String, String> catalogProperties = Map.ofEntries(
                Map.entry("type", catalogType),
                Map.entry("catalog-impl", catalogImpl),
                Map.entry("io-impl", catalogIOImpl),
                Map.entry("uri", jdbcUri),
                Map.entry("jdbc.driver", jdbcDriver),
                Map.entry("jdbc.user", jdbcUser),
                Map.entry("jdbc.password", jdbcPassword),
                Map.entry("warehouse", warehousePath),
                Map.entry("s3.endpoint", s3Endpoint),
                Map.entry("s3.access-key-id", s3AccessKey),
                Map.entry("s3.secret-access-key", s3SecretKey),
                Map.entry("s3.path-style-access", s3PathStyle),
                Map.entry("client.region", s3ClientRegion)
        );


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(flinkConf);

        // By default, Flink will use the JobManager’s heap;
        // in my case checkpoints will be stored in S3 bucket
        env.enableCheckpointing(CHECKPOINT_INTERVAL);

        env.getCheckpointConfig().setExternalizedCheckpointRetention(ExternalizedCheckpointRetention.RETAIN_ON_CANCELLATION);

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(brokers)
                .setTopics(inputTopic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        // WatermarkStrategy.forBoundedOutOfOrderness(WATERMARK_BOUND_DURATION),

        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy
                .<String>forBoundedOutOfOrderness(WATERMARK_BOUND_DURATION)
                .withTimestampAssigner((record, ts) -> {
                    try {

                        LOG.info("ABC");

                        JsonNode jsonNode = objectMapper.readTree(record);
                        String eventDateTime = jsonNode.get("datetime").asText();
                        return new SimpleDateFormat(DATETIME_FORMAT).parse(eventDateTime).toInstant().toEpochMilli();
                    } catch (Exception e) {
                        return System.currentTimeMillis();
                    }
                })
                .withIdleness(WATERMARK_IDLENESS_DURATION);

        DataStream<String> stream = env.fromSource(
                kafkaSource,
                watermarkStrategy,
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
                new org.apache.hadoop.conf.Configuration(false),
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

        Catalog catalog = catalogLoader.loadCatalog();
        LOG.info("Iceberg Table for streaming write {}", catalog.listTables(Namespace.of(rawZoneDatabase)));

        BenchmarkMessageRowDataMapper mapper = new BenchmarkMessageRowDataMapper();
        BenchmarkMessageIcebergSink benchmarkMessageIcebergSink = new BenchmarkMessageIcebergSink();

        benchmarkMessageIcebergSink.sinkToIceberg(
                parsedStream,
                tableLoader,
                mapper
        );

        env.execute("Kafka-to-Iceberg Streaming Job");
    }


}
