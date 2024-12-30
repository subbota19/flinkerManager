FROM flink:1.19.0

ENV pyVersion=3.8.10
# Install Kafka connector dependencies
RUN mkdir -p /opt/flink/usrlib
RUN wget -P /opt/flink/usrlib https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-kafka_2.12/1.14.6/flink-connector-kafka_2.12-1.14.6.jar

# Install Python
RUN apt-get update -y && \
apt-get install -y build-essential libssl-dev zlib1g-dev libbz2-dev liblzma-dev libffi-dev python3-pip nano && \
wget https://www.python.org/ftp/python/$pyVersion/Python-$pyVersion.tgz && \
tar -xvf Python-$pyVersion.tgz && cd Python-$pyVersion && \
./configure --without-tests --enable-shared && make -j6 && \
make install && ldconfig /usr/local/lib && cd .. && rm -f Python-$pyVersion.tgz && rm -rf Python-$pyVersion && \
ln -s /usr/local/bin/python3 /usr/local/bin/python && \
apt-get clean && rm -rf /var/lib/apt/lists/*

# Download the files using wget
RUN wget https://files.pythonhosted.org/packages/51/34/39cd723c7de2c8e4278fc8809f2a4f6c64787aab9e8ad0358fff1e91de0c/apache-flink-1.19.1.tar.gz
RUN wget https://files.pythonhosted.org/packages/6c/69/b75c152878d45e2727d29d50178eb4b23ccbb0844a60620ce630045dc0ef/apache-flink-libraries-1.19.1.tar.gz

# Install the downloaded packages using pip3

RUN pip3 install --upgrade pip && pip3 install apache-flink-libraries-1.19.1.tar.gz && pip3 install apache-flink-1.19.1.tar.gz

# Clean up
RUN rm apache-flink-1.19.1.tar.gz apache-flink-libraries-1.19.1.tar.gz

WORKDIR /src

COPY src/ .





