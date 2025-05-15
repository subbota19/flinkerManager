FROM apache/gravitino-iceberg-rest:latest

ARG POSTGRESQL_VERSION=42.7.1
ARG ICEBERG_VERSION=1.8.1

WORKDIR /root/gravitino-iceberg-rest-server

RUN mkdir -p custom_conf

COPY conf/custom-gravitino-iceberg-rest-server.conf custom_conf/gravitino-iceberg-rest-server.conf

RUN cp conf/core-site.xml custom_conf/core-site.xml && \
    cp conf/hdfs-site.xml custom_conf/hdfs-site.xml && \
    cp conf/gravitino-env.sh custom_conf/gravitino-env.sh && \
    cp conf/log4j2.properties custom_conf/log4j2.properties

RUN curl -o libs/postgresql-${POSTGRESQL_VERSION}.jar  \
    https://jdbc.postgresql.org/download/postgresql-${POSTGRESQL_VERSION}.jar

RUN curl -o libs/iceberg-aws-bundle-${ICEBERG_VERSION}.jar  \
    https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-aws-bundle/${ICEBERG_VERSION}/iceberg-aws-bundle-${ICEBERG_VERSION}.jar

ENTRYPOINT ["./bin/gravitino-iceberg-rest-server.sh", "--config", "/root/gravitino-iceberg-rest-server/custom_conf", "restart"]
