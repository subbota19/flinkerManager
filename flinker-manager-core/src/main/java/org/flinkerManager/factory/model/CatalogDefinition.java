package org.flinkerManager.factory.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CatalogDefinition {
    public String name;
    public String type;

    @JsonProperty("catalog-impl")
    public String catalogImpl;

    @JsonProperty("io-impl")
    public String ioImpl;

    public String uri;

    @JsonProperty("jdbc.driver")
    public String jdbcDriver;

    public String warehouse;
}
