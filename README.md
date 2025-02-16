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

`minikube mount /home/yauheni/main/codeDomain/flinkerManager/mount/plugins:/mnt/data/flink/plugins`

`minikube mount /home/yauheni/main/codeDomain/flinkerManager/mount/plugins:/mnt/target`