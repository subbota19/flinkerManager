package org.flinkerManager.mappers;

import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.types.RowKind;
import org.flinkerManager.jobs.KafkaIcebergDataStreamJob;
import org.flinkerManager.models.BenchmarkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkMessageRowDataMapper implements RowDataMapper<BenchmarkMessage> {

    @Override
    public RowData map(BenchmarkMessage message) {
        GenericRowData rowData = new GenericRowData(RowKind.INSERT, 7);
        rowData.setField(0, message.getId());
        rowData.setField(1, StringData.fromString(message.getType()));
        rowData.setField(2, StringData.fromString(message.getMessage()));
        rowData.setField(3, StringData.fromString(message.getRawDatetime()));
        rowData.setField(4, message.getProcessId());
        rowData.setField(5, message.getClientId());
        rowData.setField(6, message.getProcessedDatetime());
        return rowData;
    }
}
