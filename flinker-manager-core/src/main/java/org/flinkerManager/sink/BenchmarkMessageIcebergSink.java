package org.flinkerManager.sink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.flink.TableLoader;
import org.apache.flink.table.data.RowData;
import org.flinkerManager.models.BenchmarkMessage;
import org.flinkerManager.mappers.RowDataMapper;

import java.util.List;

public class BenchmarkMessageIcebergSink implements IcebergSink<BenchmarkMessage> {

    @Override
    public void sinkToIceberg(
            DataStream<BenchmarkMessage> dataStream,
            TableLoader tableLoader,
            RowDataMapper<BenchmarkMessage> mapper
    ) {
        DataStream<RowData> rowDataStream = dataStream.map(mapper::map);

        FlinkSink.forRowData(rowDataStream)
                .tableLoader(tableLoader)
                .upsert(false)
                .append();
    }
}
