package org.flinkerManager.factory.model;

public class MetadataCompactionDefinition {
    private boolean enabled = false;
    private String rewriteIf;
    private Integer specId;
    private String stagingLocation;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRewriteIf() {
        return rewriteIf;
    }

    public void setRewriteIf(String rewriteIf) {
        this.rewriteIf = rewriteIf;
    }

    public Integer getSpecId() {
        return specId;
    }

    public void setSpecId(Integer specId) {
        this.specId = specId;
    }

    public String getStagingLocation() {
        return stagingLocation;
    }

    public void setStagingLocation(String stagingLocation) {
        this.stagingLocation = stagingLocation;
    }

}
