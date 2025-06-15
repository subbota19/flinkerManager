package org.flinkerManager.factory.model;

public class DataCompactionDefinition {
    private boolean enabled = false;
    private Integer targetSizeInBytes;
    private Integer splitLookback;
    private Integer splitOpenFileCost;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getTargetSizeInBytes() {
        return targetSizeInBytes;
    }

    public void setTargetSizeInBytes(Integer targetSizeInBytes) {
        this.targetSizeInBytes = targetSizeInBytes;
    }

    public Integer getSplitLookback() {
        return splitLookback;
    }

    public void setSplitLookback(Integer splitLookback) {
        this.splitLookback = splitLookback;
    }

    public Integer getSplitOpenFileCost() {
        return splitOpenFileCost;
    }

    public void setSplitOpenFileCost(Integer splitOpenFileCost) {
        this.splitOpenFileCost = splitOpenFileCost;
    }


}
