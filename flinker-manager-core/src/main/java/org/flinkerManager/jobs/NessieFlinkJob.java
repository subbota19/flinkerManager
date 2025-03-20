package org.flinkerManager.jobs;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.flinkerManager.builder.QueryBuilder;
import org.flinkerManager.models.BenchmarkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.Duration;
import java.util.Map;


public class NessieFlinkJob {
    private static final Logger LOG = LoggerFactory.getLogger(NessieFlinkJob.class);


    public static void main(String[] args) throws Exception {

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 2);
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofMinutes(1));


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(
                env, EnvironmentSettings.newInstance().inStreamingMode().build()
        );


        Map<String, String> catalogProperties = Map.ofEntries(
                Map.entry("type", "iceberg"),
                Map.entry("catalog-impl", "org.apache.iceberg.nessie.NessieCatalog"),
                Map.entry("io-impl", "org.apache.iceberg.aws.s3.S3FileIO"),
                Map.entry("uri", "http://nessie-service:19120/api/v1"),
                Map.entry("ref", "main"),
                Map.entry("client.assume-role.region", "us-east-1"),
                Map.entry("warehouse", "s3://datalake"),
                Map.entry("s3.endpoint", "http://minio:9000"),
                Map.entry("s3.access-key-id", "ADMIN"),
                Map.entry("s3.secret-access-key", "12345678"),
                Map.entry("s3.path-style-access", "true"),
                Map.entry("client.region", "us-east-1")
        );

        QueryBuilder sqlBuilder = new QueryBuilder()
                .createCatalog("iceberg_catalog", catalogProperties)
                .useCatalog("iceberg_catalog")
                .createDatabase("raw_zone")
                .useDatabase("raw_zone")
                .createTableFromModel("benchmark_message", BenchmarkMessage.class);


        for (String sql : sqlBuilder.build().split(";")) {
            if (!sql.trim().isEmpty()) {
                LOG.info("Executing SQL: " + sql);
                System.out.println(sql);
                tableEnv.executeSql(sql);
            }
        }

        LOG.info("SQL Execution Completed!");

    }
}
