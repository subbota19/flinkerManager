package org.flinkerManager.jobs;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.iceberg.catalog.Catalog;
import org.flinkerManager.factory.service.CatalogCreator;
import org.flinkerManager.factory.service.TableCreator;

public class CreateIcebergTableJob {

    public static void main(String[] args) throws Exception {
        ParameterTool params = ParameterTool.fromArgs(args);

        String catalogYamlPath = params.getRequired("catalog.config.path");
        String tableYamlPath = params.getRequired("table.config.path");

        CatalogCreator catalogCreator = new CatalogCreator(catalogYamlPath);
        Catalog catalog = catalogCreator.loadCatalog();

        TableCreator tableCreator = new TableCreator(tableYamlPath);
        tableCreator.createTable(catalog);
    }
}
