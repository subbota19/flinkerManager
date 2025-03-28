# FlinkerManager

This project serves as a personal lab for developing and honing skills in distributed data processing and data lake architecture:

* Flink Cluster Experimentation: To establish a Flink cluster on Minikube and explore the impact of varying configurations and workloads. I'm mostly following the official Flink Kubernetes [docs](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/kubernetes/).
* Data Lake Implementation: To construct a basic data lake utilizing Apache Iceberg, enabling data ingestion via Flink and subsequent querying with open-source query engines.
* Transactional Catalog: To gain experience with data version control using Nessie catalog.
* Kafka Integration: To integrate an existing Kafka cluster as a data source simulating real-time data ingestion.

[Kafka project](https://github.com/subbota19/kafkaInfra)

[Event Stream Generation project](https://github.com/subbota19/msgGeneratorKafka)

## Construct project skeleton

`mvn archetype:generate
-DarchetypeGroupId=org.apache.flink
-DarchetypeArtifactId=flink-quickstart-java
-DarchetypeVersion=1.20.0`

## Build project

`mvn clean package -Pbuild-jar`

## Local Testing

`./bin/start-cluster`

`./bin/flink run -detached <path to JAR>`

## Mount Processes

I've found that new Flink plugins/libs are sometimes necessary. One method to add is to use minikube mount, connecting it to the Flink cluster. All
additional plugins/libs are stored at /mount. To build the TaskManager pod correctly, activate the mount with this
command:

`minikube mount /home/yauheni/main/codeDomain/flinkerManager/mount/utils:/mnt/data/flink/utils`

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

`docker build --tag custom_flink .`

`minikube image load custom_flink:latest`

and don't forgot to include:

          imagePullPolicy: Never

For pushing image:

`docker login`

`docker tag custom_flink:latest subbota19/custom_flink:latest`

`docker push subbota19/custom_flink:latest`

### Rationale:

This custom Flink Docker image is built to provide a flexible development environment. By building a custom image, you gain the ability to:

* Installing tools for easy troubleshooting and checking connectivity between pods in CLI.
* Add Java libraries directly into the image for specific application requirements.

## Check UI

Provide minikube IP

* Flink cluster: http://{minikube ip}:30900

* MiniO: http://{minikube ip}:30801

* Nessie: http://{minikube ip}:30920

## Check specific class in JAR

`jar tvf target/flinker-manager-core-1.0-SNAPSHOT.jar | grep hadoop/conf/Configuration.class`

`jar tvf target/flinker-manager-core-1.0-SNAPSHOT.jar | grep flink/runtime/util/HadoopUtils.class`

## Check connectivity

`kubectl exec -it jobmanager -- curl -I http://nessie-service:19120/api/v1/trees/tree`

## Run job on the cluster

`./bin/flink run -c org.flinkerManager.jobs.NessieFlinkJob /opt/flink/examples/jars/flinker-manager-core-1.0-SNAPSHOT.jar`

## Flink configuration

**flink-configuration-configmap.yaml** is a custom YAML config file for Flink cluster parameters. Please modify it if you require a custom setup:

    env.hadoop.conf.dir: /opt/hadoop/hadoop-3.4.1/etc/hadoop

## Minio Client commands

`mc mb minio/warehouse;`

`mc anonymous set public minio/warehouse;`
