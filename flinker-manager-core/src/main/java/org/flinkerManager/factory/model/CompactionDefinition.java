package org.flinkerManager.factory.model;

public class CompactionDefinition {
    private DataCompactionDefinition data;
    private MetadataCompactionDefinition metadata;

    public DataCompactionDefinition getData() {
        return data;
    }

    public void setData(DataCompactionDefinition data) {
        this.data = data;
    }

    public MetadataCompactionDefinition getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataCompactionDefinition metadata) {
        this.metadata = metadata;
    }
}
