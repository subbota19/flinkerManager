package org.flinkerManager.factory.model;

public class PartitionDefinition {
    private String name;
    private String transform;

    public PartitionDefinition() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransform() {
        return transform;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }
}
