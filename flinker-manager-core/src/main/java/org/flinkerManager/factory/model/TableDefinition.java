package org.flinkerManager.factory.model;

import java.util.List;
import java.util.Map;

public class TableDefinition {
    private String name;

    private String namespace;
    private List<ColumnDefinition> schema;
    private List<PartitionDefinition> partition;
    private Map<String, String> properties;
    private CompactionDefinition compaction;

    public TableDefinition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnDefinition> getSchema() {
        return schema;
    }

    public void setSchema(List<ColumnDefinition> schema) {
        this.schema = schema;
    }

    public List<PartitionDefinition> getPartition() {
        return partition;
    }

    public void setPartition(List<PartitionDefinition> partition) {
        this.partition = partition;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public CompactionDefinition getCompaction() {
        return compaction;
    }

    public void setCompaction(CompactionDefinition compaction) {
        this.compaction = compaction;
    }


}
