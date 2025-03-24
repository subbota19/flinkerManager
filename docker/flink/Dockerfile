FROM flink:1.20.0

ARG HADOOP_VERSION=3.4.0
ARG FLINK_VERSION=1.20.0
ARG ICEBERG_VERSION=1.8.1
ARG HADOOP_UBER_VERSION=2.8.3-10.0
ARG POSTGRESQL_VERSION=42.7.1

RUN apt update && apt install -y nano iputils-ping

RUN curl -sSL https://dl.min.io/client/mc/release/linux-amd64/mc --create-dirs -o /usr/local/bin/mc && \
    chmod +x /usr/local/bin/mc

RUN curl -o /opt/flink/lib/flink-s3-fs-hadoop-${FLINK_VERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/flink/flink-s3-fs-hadoop/${FLINK_VERSION}/flink-s3-fs-hadoop-${FLINK_VERSION}.jar &&  \
    chown flink:flink /opt/flink/lib/flink-s3-fs-hadoop-${FLINK_VERSION}.jar

RUN curl -o /opt/flink/lib/hadoop-common-${HADOOP_VERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-common/${HADOOP_VERSION}/hadoop-common-${HADOOP_VERSION}.jar &&  \
    chown -R flink:flink /opt/flink/lib/hadoop-common-${HADOOP_VERSION}.jar

RUN curl -o /opt/flink/lib/flink-shaded-hadoop-2-uber-${HADOOP_UBER_VERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_UBER_VERSION}/flink-shaded-hadoop-2-uber-${HADOOP_UBER_VERSION}.jar &&  \
    chown -R flink:flink /opt/flink/lib/flink-shaded-hadoop-2-uber-${HADOOP_UBER_VERSION}.jar

RUN mkdir -p /opt/flink/lib/jdbc
RUN curl -o /opt/flink/lib/jdbc/postgresql-${POSTGRESQL_VERSION}.jar \
    https://jdbc.postgresql.org/download/postgresql-${POSTGRESQL_VERSION}.jar &&  \
    chown -R flink:flink /opt/flink/lib/jdbc/postgresql-${POSTGRESQL_VERSION}.jar
