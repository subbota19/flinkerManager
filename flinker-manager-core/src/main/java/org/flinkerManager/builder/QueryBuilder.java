package org.flinkerManager.builder;


import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {
    private final StringBuilder query;

    public QueryBuilder() {
        this.query = new StringBuilder();
    }

    public QueryBuilder createCatalog(String catalogName, Map<String, String> properties) {
        query.append("CREATE CATALOG ").append(catalogName).append(" WITH (");
        properties.forEach((key, value) -> query.append("'").append(key).append("'='").append(value).append("',"));
        query.setLength(query.length() - 1); // Remove last comma
        query.append(");\n");
        return this;
    }

    public QueryBuilder useCatalog(String catalogName) {
        query.append("USE CATALOG ").append(catalogName).append(";\n");
        return this;
    }

    public QueryBuilder createDatabase(String databaseName) {
        query.append("CREATE DATABASE IF NOT EXISTS ").append(databaseName).append(";\n");
        return this;
    }

    public QueryBuilder useDatabase(String databaseName) {
        query.append("USE ").append(databaseName).append(";\n");
        return this;
    }

    public QueryBuilder createTable(String tableName, Map<String, String> columns) {
        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        columns.forEach((column, type) -> query.append(column).append(" ").append(type).append(", "));
        query.setLength(query.length() - 2); // Remove last comma
        query.append(");\n");
        return this;
    }

    public QueryBuilder createTableFromModel(String tableName, Class<?> modelClass) {
        Map<String, String> fieldMappings = getFieldMappings(modelClass);

        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        fieldMappings.forEach((fieldName, sqlType) ->
                query.append(fieldName).append(" ").append(sqlType).append(", ")
        );

        query.setLength(query.length() - 2); // Remove last comma
        query.append(");\n");

        return this;
    }

    public QueryBuilder removeTable(String tableName) {
        query.append("DROP TABLE IF EXISTS ").append(tableName).append(";\n");
        return this;
    }

    private static Map<String, String> getFieldMappings(Class<?> modelClass) {
        Map<String, String> fieldMappings = new LinkedHashMap<>();

        for (Field field : modelClass.getDeclaredFields()) {
            String sqlType = mapJavaTypeToSQL(field.getType());
            fieldMappings.put(field.getName(), sqlType);
        }

        return fieldMappings;
    }

    private static String mapJavaTypeToSQL(Class<?> javaType) {
        if (javaType == String.class) return "STRING";
        if (javaType == int.class || javaType == Integer.class) return "INT";
        if (javaType == long.class || javaType == Long.class) return "BIGINT";
        if (javaType == double.class || javaType == Double.class) return "DOUBLE";
        if (javaType == float.class || javaType == Float.class) return "FLOAT";
        if (javaType == boolean.class || javaType == Boolean.class) return "BOOLEAN";
        return "STRING";
    }

    public QueryBuilder insertInto(String tableName, List<Object[]> values) {
        query.append("INSERT INTO ").append(tableName).append(" VALUES ");
        for (Object[] row : values) {
            query.append("(");
            for (Object value : row) {
                if (value instanceof String) {
                    query.append("'").append(value).append("', ");
                } else {
                    query.append(value).append(", ");
                }
            }
            query.setLength(query.length() - 2); // Remove last comma
            query.append("), ");
        }
        query.setLength(query.length() - 2); // Remove last comma
        query.append(";\n");
        return this;
    }

    public String build() {
        return query.toString();
    }
}
