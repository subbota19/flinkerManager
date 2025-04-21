package org.flinkerManager.sink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.iceberg.flink.TableLoader;
import org.flinkerManager.mappers.RowDataMapper;

public interface IcebergSink<T> {
    void sinkToIceberg(
            DataStream<T> dataStream,
            TableLoader tableLoader,
            RowDataMapper<T> mapper
    );
}
