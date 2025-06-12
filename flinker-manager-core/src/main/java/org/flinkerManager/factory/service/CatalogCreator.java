package org.flinkerManager.factory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.flink.CatalogLoader;
import org.flinkerManager.factory.model.CatalogDefinition;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CatalogCreator {

    private final CatalogDefinition definition;
    private final Map<String, String> catalogProperties;

    public CatalogCreator(String yamlPath) throws Exception {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        this.definition = mapper.readValue(new File(yamlPath), CatalogDefinition.class);
        this.catalogProperties = buildCatalogProperties();
    }

    public Catalog loadCatalog() {
        CatalogLoader catalogLoader = CatalogLoader.custom(
                definition.name,
                catalogProperties,
                new org.apache.hadoop.conf.Configuration(false),
                definition.catalogImpl
        );

        Catalog catalog = catalogLoader.loadCatalog();
        System.out.printf("Catalog loaded: %s%n", catalog.name());
        return catalog;
    }

    public Map<String, String> getCatalogProperties() {
        return catalogProperties;
    }

    private Map<String, String> buildCatalogProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("type", definition.type);
        props.put("catalog-impl", definition.catalogImpl);
        props.put("io-impl", definition.ioImpl);
        props.put("uri", definition.uri);
        props.put("jdbc.driver", definition.jdbcDriver);
        props.put("warehouse", definition.warehouse);

        String jdbcUser = System.getenv("JDBC_USER");
        String jdbcPassword = System.getenv("JDBC_PASSWORD");

        if (jdbcUser != null) props.put("jdbc.user", jdbcUser);
        if (jdbcPassword != null) props.put("jdbc.password", jdbcPassword);

        props.putAll(loadS3SettingsFromFlinkGlobal());

        return props;
    }

    private Map<String, String> loadS3SettingsFromFlinkGlobal() {
        Map<String, String> s3Settings = new HashMap<>();

        Configuration globalConf = GlobalConfiguration.loadConfiguration();

        String s3AccessKey = globalConf.getString("s3.access-key", null);
        String s3SecretKey = globalConf.getString("s3.secret-key", null);
        String s3Endpoint = globalConf.getString("s3.endpoint", null);
        String s3PathStyle = globalConf.getString("s3.path.style.access", "true");
        String s3ClientRegion = globalConf.getString("s3.client.region", "us-east-1");

        if (s3AccessKey != null) s3Settings.put("s3.access-key-id", s3AccessKey);
        if (s3SecretKey != null) s3Settings.put("s3.secret-access-key", s3SecretKey);
        if (s3Endpoint != null) s3Settings.put("s3.endpoint", s3Endpoint);
        s3Settings.put("s3.path-style-access", s3PathStyle);
        s3Settings.put("client.region", s3ClientRegion);

        return s3Settings;
    }
}
