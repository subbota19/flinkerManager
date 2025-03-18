# FlinkerManager

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

I've found that new Flink plugins/libs are sometimes necessary. In my project, instead of creating a custom Flink image,
I
opted for the standard one. To add plugins/libs, I set up a Minikube mount and connected it to the Flink cluster. All
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

## Check UI

Provide minikube IP

Flink cluster: http://192.168.49.2:30900
MiniO: http://192.168.49.2:30801/

## Check specific class in JAR

`jar tvf target/flinker-manager-core-1.0-SNAPSHOT.jar | grep hadoop/conf/Configuration.class`

`jar tvf target/flinker-manager-core-1.0-SNAPSHOT.jar | grep flink/runtime/util/HadoopUtils.class`

## Check connectivity

`kubectl exec -it jobmanager -- curl -I http://nessie-service:19120/api/v1/trees/tree`

## Run job on the cluster

`./bin/flink run -c org.flinkerManager.jobs.NessieFlinkJob /opt/flink/examples/jars/flinker-manager-core-1.0-SNAPSHOT.jar`

## Flink configuration

    env.hadoop.conf.dir: /opt/hadoop/hadoop-3.4.1/etc/hadoop

## Minio Client commands

`mc mb minio/warehouse;`

`mc anonymous set public minio/warehouse;`