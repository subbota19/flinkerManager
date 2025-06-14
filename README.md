# FlinkerManager

This project serves as a personal lab for developing and honing skills in distributed data processing and data lake
architecture:

* Flink Cluster Experimentation: To establish a Flink cluster on Minikube and explore the impact of varying
  configurations and workloads. I'm mostly following the official Flink
  Kubernetes [docs](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/kubernetes/).
* Data Lake Implementation: To construct a basic data lake utilizing Apache Iceberg, enabling data ingestion via Flink
  and subsequent querying with open-source query engines.
* Transactional Catalog: To gain experience with data version control using Nessie catalog.
* Kafka Integration: To integrate an existing Kafka cluster as a data source simulating real-time data ingestion.

[Kafka project](https://github.com/subbota19/kafkaInfra)

[Event Stream Generation project](https://github.com/subbota19/msgGeneratorKafka)

## Architecture

![image](https://github.com/user-attachments/assets/aec8ac89-76f0-4d1b-9f8f-ef19291086b5)

## Construct project skeleton

`mvn archetype:generate
-DarchetypeGroupId=org.apache.flink
-DarchetypeArtifactId=flink-quickstart-java
-DarchetypeVersion=1.20.0`

## Build project

`mvn clean package -Pbuild-jar -Dmain.class=org.flinkerManager.jobs.KafkaIcebergDataStreamJob`

## Local Testing

`./bin/start-cluster`

`./bin/flink run -detached <path to JAR>`

## Mount Processes

I've found that new Flink plugins/libs are sometimes necessary. One method to add is to use minikube mount, connecting
it to the Flink cluster. All
additional plugins/libs are stored at /mount. To build the TaskManager pod correctly, activate the mount with this
command:

`minikube mount /home/yauheni/main/codeDomain/flinkerManager/flinker-manager-core/target:/mnt/data/flink/utils/jars`

`minikube mount /home/yauheni/main/codeDomain/flinkerManager/flinker-manager-core/src/main/resources:/mnt/data/flink/utils/resources`

Kubernetes settings

          volumeMounts:
            - name: flink-plugin-flink-s3-fs-hadoop-volume
              mountPath: /opt/flink/plugins/flink-s3-fs-hadoop
            - name: flink-lib-hadoop-common
              mountPath: /opt/flink/lib/hadoop-common-3.4.1.jar

      volumes:
        - name: flink-plugin-flink-s3-fs-hadoop-volume
          hostPath:
            path: /mnt/data/flink/utils/plugins/flink-s3-fs-hadoop
        - name: flink-lib-hadoop-common
          hostPath:
            path: /mnt/data/flink/utils/libs/hadoop-common/hadoop-common-3.4.1.jar
            type: File

## Docker

For building the custom image do the next steps:

`cd docker/flink`

`docker build --tag custom_flink:1.20.0 .`

`minikube image load custom_flink:1.20.0`

and don't forgot to include:

          imagePullPolicy: Never

For pushing image:

`docker login`

`docker tag custom_flink:latest subbota19/custom_flink:latest`

`docker push subbota19/custom_flink:latest`

### Rationale:

This custom Flink Docker image is built to provide a flexible development environment. By building a custom image, you
gain the ability to:

* Installing tools for easy troubleshooting and checking connectivity between pods in CLI.
* Add Java libraries directly into the image for specific application requirements.

## Check UI

Provide minikube IP

* Flink cluster: `http://{minikube ip}:30900`

* MiniO: `http://{minikube ip}:30801`

* Nessie: `http://{minikube ip}:30920`

## Check specific class in JAR

`jar tvf target/flinker-manager-core-1.0-SNAPSHOT.jar | grep hadoop/conf/Configuration.class`

`jar tvf target/flinker-manager-core-1.0-SNAPSHOT.jar | grep flink/runtime/util/HadoopUtils.class`

## Check connectivity

`kubectl exec -it jobmanager -- curl -I http://nessie-service:19120/api/v1/trees/tree`

## Run job on the cluster

`./bin/flink run -c org.flinkerManager.jobs.KafkaIcebergDataStreamJob /opt/flink/examples/jars/flinker-manager-core-1.0-SNAPSHOT.jar`

`./bin/flink run -c org.flinkerManager.jobs.CreateIcebergTableJob /opt/flink/examples/jars/flinker-manager-core-1.0-SNAPSHOT.jar --catalog.config.path /opt/flink/examples/resources/catalogs/iceberg_catalog.yaml --table.config.path /opt/flink/examples/resources/tables/benchmark_message_cow.yaml`

`./bin/flink run -c org.flinkerManager.jobs.CreateIcebergTableJob /opt/flink/examples/jars/flinker-manager-core-1.0-SNAPSHOT.jar --catalog.config.path /opt/flink/examples/resources/catalogs/iceberg_catalog.yaml --table.config.path /opt/flink/examples/resources/tables/benchmark_message_mor.yaml`

## Flink configuration

**flink-configuration-configmap.yaml** is a custom YAML config file for Flink cluster parameters. Please modify it if
you require a custom setup:

    env.hadoop.conf.dir: /opt/hadoop/hadoop-3.4.1/etc/hadoop

## Minio Client commands

`mc mb minio/warehouse;`

`mc anonymous set public minio/warehouse;`

## Verify minikube RAM/CPU consumption

`docker stats minikube`

## Adjust minikube RAM/CPU

`minikube config set memory 10000`
`minikube config set cpus 2`

## Secret manager

`kubectl create secret generic postgres-secret -n datalakehouse \
--from-literal=user=postgres \
--from-literal=password=postgres \
--from-literal=admin_password=ADMIN \
--from-literal=admin_email=admin@admin.com`

`kubectl create secret generic minio-secret -n datalakehouse \
--from-literal=user=ADMIN \
--from-literal=password=12345678`

## Gravitino API endpoints

`{host}:{port}/iceberg/v1/namespaces/raw_zone/tables/benchmark_message`

## Gravitino commands

`./bin/gravitino-iceberg-rest-server.sh start --confing /root/gravitino-iceberg-rest-server/conf/gravitino-iceberg-rest-server.conf`

## Validate gravitino incoming HTTP requests

`tcpdump -i any -A port 9010`

## Flink SQL

`./bin/sql-client.sh`

## Starrocks commands

`kubectl exec -it starrocks-fe-0 -n query -- mysql -P 9030 -h 127.0.0.1 -u root`
