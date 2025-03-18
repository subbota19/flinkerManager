FROM flink:1.20.0

ARG HADOOP_VERSION=3.4.0

RUN apt-get update && apt-get install -y wget tar && \
    mkdir -p /opt/hadoop && \
    cd /opt/hadoop && \
    wget -q https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz && \
    tar -xvf hadoop-${HADOOP_VERSION}.tar.gz && \
    rm hadoop-${HADOOP_VERSION}.tar.gz

ENV HADOOP_HOME=/opt/hadoop/hadoop-${HADOOP_VERSION}
ENV PATH="$HADOOP_HOME/bin:$PATH"
ENV HADOOP_CLASSPATH="$(${HADOOP_HOME}/bin/hadoop classpath)"

RUN chown -R flink:flink $HADOOP_HOME

USER flink

RUN echo "Hadoop installed at: $HADOOP_HOME" && \
    echo "Hadoop classpath: $HADOOP_CLASSPATH"