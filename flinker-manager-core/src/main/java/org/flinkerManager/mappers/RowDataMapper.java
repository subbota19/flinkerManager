package org.flinkerManager.mappers;

import org.apache.flink.table.data.RowData;

import java.io.Serializable;

public interface RowDataMapper<T> extends Serializable {
    RowData map(T value);
}
