package org.flinkerManager.jobs;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

public class S3FlinkJob {
    private static final Logger LOG = LoggerFactory.getLogger(S3FlinkJob.class);

    public static void main(String[] args) throws Exception {

        String bucketName = "flink-test-bucket-" + UUID.randomUUID();
        String fileName = "test-file.txt";
        String content = "Test: Flink-MinIO";
        String accessKeyId = "ADMIN";
        String secretAccessKey = "12345678";


        S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("http://minio:9000"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .forcePathStyle(true)
                .build();

        s3.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build());

        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build(),
                RequestBody.fromString(content, StandardCharsets.UTF_8));

        System.out.println("File uploaded to MinIO bucket " + bucketName);

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 1);
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofMinutes(1));

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);

        DataStreamSource<String> stream = env.readTextFile("s3://" + bucketName + "/" + fileName);

        stream.map(line -> {
            LOG.info("Read from MinIO: " + line);
            return line;
        });

        env.execute("Flink MinIO File Processing");
    }
}
