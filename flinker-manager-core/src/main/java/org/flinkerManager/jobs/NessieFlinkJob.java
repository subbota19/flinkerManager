package org.flinkerManager.jobs;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;


public class NessieFlinkJob {

    public static void main(String[] args) throws Exception {


        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(
                env, EnvironmentSettings.newInstance().inStreamingMode().build()
        );


        tableEnv.executeSql(
                "CREATE CATALOG iceberg WITH ("
                        + "'type'='iceberg',"
                        + "'catalog-impl'='org.apache.iceberg.nessie.NessieCatalog',"
                        + "'io-impl'='org.apache.iceberg.aws.s3.S3FileIO',"
                        + "'uri'='http://nessie-service:19120/api/v1',"
                        + "'ref'='main',"
                        + "'client.assume-role.region'='us-east-1',"
                        + "'warehouse' = 's3://warehouse',"
                        + "'s3.endpoint'='http://minio:9000',"
                        + "'s3.access-key-id' = 'ADMIN',"
                        + "'s3.secret-access-key'='12345678',"
                        + "'s3.path-style-access'='true',"
                        + "'client.region'='us-east-1'"
                        + ")");

        TableResult result = tableEnv.executeSql("SHOW CATALOGS");

        result.print();

        tableEnv.useCatalog("iceberg");

        tableEnv.executeSql("CREATE DATABASE IF NOT EXISTS db");

        tableEnv.executeSql(
                "CREATE TABLE IF NOT EXISTS db.test ("
                        + "id BIGINT COMMENT 'unique id',"
                        + "data STRING"
                        + ")");

        tableEnv.executeSql(
                "INSERT INTO db.test VALUES "
                        + "(1, 'Hello Iceberg'), "
                        + "(2, 'Flink SQL Test'), "
                        + "(3, 'Sample Data')");
    }
}
