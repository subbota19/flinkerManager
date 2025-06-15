package org.flinkerManager.archive.jobs;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.*;
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


public class IcebergPostgresFlinkJob {
    private static final Logger LOG = LoggerFactory.getLogger(IcebergPostgresFlinkJob.class);


    public static void main(String[] args) throws Exception {

        ParameterTool params = ParameterTool.fromArgs(args);

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

        Configuration globalConf = GlobalConfiguration.loadConfiguration();

        String s3AccessKey = globalConf.getString("s3.access-key", null);
        String s3SecretKey = globalConf.getString("s3.secret-key", null);
        String s3Endpoint = globalConf.getString("s3.endpoint", null);
        String s3PathStyle = globalConf.getString("s3.path.style.access", "true");
        String s3ClientRegion = globalConf.getString("s3.client.region", "us-east-1");

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 2);
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofMinutes(1));


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(
                env, EnvironmentSettings.newInstance().inStreamingMode().build()
        );

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

        QueryBuilder sqlBuilder = new QueryBuilder()
                .createCatalog(catalogName, catalogProperties)
                .useCatalog(catalogName)
                .useDatabase(rawZoneDatabase)
                .createDatabase(rawZoneDatabase)
                .removeTable(benchmarkMessageTable)
                .createTableFromModel(benchmarkMessageTable, BenchmarkMessage.class);


        for (String sql : sqlBuilder.build().split(";")) {
            if (!sql.trim().isEmpty()) {
                LOG.info("Executing SQL: " + sql);
                System.out.println(sql);
                tableEnv.executeSql(sql).print();
            }
        }

        LOG.info("SQL Execution Completed!");

    }
}
