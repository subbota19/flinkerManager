package org.flinkerManager.factory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.iceberg.*;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.flinkerManager.factory.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableCreator {

    private final TableDefinition definition;

    public TableCreator(String tableYamlPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        this.definition = mapper.readValue(new File(tableYamlPath), TableDefinition.class);
    }

    public void createTable(Catalog catalog, Boolean enabledCompaction) {
        TableIdentifier tableId = TableIdentifier.of(Namespace.of(definition.getNamespace()), definition.getName());

        if (!catalog.tableExists(tableId)) {
            Schema schema = buildSchema(definition.getSchema());
            PartitionSpec spec = buildPartitionSpec(schema, definition.getPartition());
            Map<String, String> properties = definition.getProperties() != null ? new HashMap<>(definition.getProperties()) : new HashMap<>();
            Table table = catalog.createTable(tableId, schema, spec, properties);
            System.out.printf("Created table: %s%n", table.name());
        } else {
            System.out.printf("Table already exists: %s%n", tableId);
            Table table = catalog.loadTable(tableId);
            CompactionDefinition compaction = definition.getCompaction();
            updateProperties(table);
            updateSchema(table);
            updatePartition(table);
            table.expireSnapshots().commit();

            if (enabledCompaction && compaction != null) {

                System.out.println("Compaction enabled via YAML config. Running compaction...");
                DataCompactionDefinition dataCompactionDefinition = compaction.getData();
                MetadataCompactionDefinition metadataCompactionDefinition = compaction.getMetadata();
                CompactionService compactionService = new CompactionService();

                if (dataCompactionDefinition != null && dataCompactionDefinition.getEnabled()) {
                    compactionService.compactData(table, dataCompactionDefinition);
                }
                if (metadataCompactionDefinition != null && metadataCompactionDefinition.getEnabled()) {
                    compactionService.compactMetadata(table, metadataCompactionDefinition);
                }

            } else {
                System.out.println("Compaction disabled or missing in YAML config.");
            }
        }
    }

    private void updateProperties(Table table) {
        Map<String, String> currentProperties = table.properties();
        Map<String, String> desiredProperties = definition.getProperties();
        UpdateProperties update = table.updateProperties();
        boolean isChanged = false;

        for (Map.Entry<String, String> entry : desiredProperties.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String currentValue = currentProperties.get(key);

            if (!newValue.equals(currentValue) && !key.equals("format-version")) {
                update.set(key, newValue);
                System.out.printf("Updated property: %s = %s%n", key, newValue);
                isChanged = true;
            }
        }

        for (String key : currentProperties.keySet()) {
            if (!desiredProperties.containsKey(key)) {
                update.remove(key);
                System.out.printf("Removed property: %s%n", key);
                isChanged = true;
            }
        }

        if (!isChanged) {
            System.out.println("No property changes needed.");
        } else {
            update.commit();
            System.out.println("Property are updated.");
        }
    }

    private void updateSchema(Table table) {
        Schema currentSchema = table.schema();
        Schema desiredSchema = buildSchema(definition.getSchema());

        Map<String, Type> currentFields = extractFieldTypes(currentSchema);
        Map<String, Type> desiredFields = extractFieldTypes(desiredSchema);

        boolean isChanged = false;
        UpdateSchema update = table.updateSchema();

        for (String colName : desiredFields.keySet()) {
            if (!currentFields.containsKey(colName)) {
                Type type = desiredFields.get(colName);
                update.addColumn(null, colName, type);
                System.out.printf("Will add column: %s (%s)%n", colName, type);
                isChanged = true;
            }
        }

        for (String colName : currentFields.keySet()) {
            if (!desiredFields.containsKey(colName)) {
                update.deleteColumn(colName);
                System.out.printf("Will delete column: %s%n", colName);
                isChanged = true;
            }
        }

        for (String colName : desiredFields.keySet()) {
            if (currentFields.containsKey(colName)) {
                Type currentType = currentFields.get(colName);
                Type desiredType = desiredFields.get(colName);
                if (!currentType.equals(desiredType)) {
                    if (isSafePromotion(currentType, desiredType)) {
                        update.updateColumn(colName, (Type.PrimitiveType) desiredType);
                        System.out.printf("Will promote column: %s (%s → %s)%n", colName, currentType, desiredType);
                        isChanged = true;
                    } else {
                        System.err.printf("Cannot safely change type of column '%s' from %s to %s%n", colName, currentType, desiredType);
                    }
                }
            }
        }

        if (isChanged) {
            update.commit();
            System.out.printf("Schema updated for table %s%n", table.name());
        } else {
            System.out.println("No schema changes needed.");
        }
    }

    private void updatePartition(Table table) {

        Schema desiredSchema = buildSchema(definition.getSchema());

        PartitionSpec currentSpec = table.spec();
        PartitionSpec desiredPartitions = buildPartitionSpec(desiredSchema, definition.getPartition());

        Map<String, String> currentFields = extractPartitionFieldMap(currentSpec);
        Map<String, String> desiredFields = extractPartitionFieldMap(desiredPartitions);

        boolean changed = false;
        UpdatePartitionSpec update = table.updateSpec();

        for (Map.Entry<String, String> entry : desiredFields.entrySet()) {
            String name = entry.getKey();
            String transform = entry.getValue();

            if (!currentFields.containsKey(name)) {
                addTransform(update, name, transform);
                System.out.printf("Added partition field: %s (%s)%n", name, transform);
                changed = true;
            }
        }

        for (String currentName : currentFields.keySet()) {
            if (!desiredFields.containsKey(currentName)) {
                update.removeField(currentName);
                System.out.printf("Removed partition field: %s%n", currentName);
                changed = true;
            }
        }

        if (changed) {
            update.commit();
            System.out.println("Updated partition spec.");
        } else {
            System.out.println("No partition spec changes needed.");
        }
    }


    private static Map<String, Type> extractFieldTypes(Schema schema) {
        Map<String, Type> fieldMap = new HashMap<>();
        schema.columns().forEach(f -> fieldMap.put(f.name(), f.type()));
        return fieldMap;
    }

    private Map<String, String> extractPartitionFieldMap(PartitionSpec spec) {
        Map<String, String> fieldMap = new HashMap<>();
        spec.fields().forEach(f -> fieldMap.put(f.name(), f.transform().toString().toLowerCase()));
        return fieldMap;
    }

    private boolean isSafePromotion(Type current, Type target) {
        if (current == Types.IntegerType.get() && target == Types.LongType.get()) return true;
        if (current == Types.FloatType.get() && target == Types.DoubleType.get()) return true;
        return (current.equals(target));
    }

    private Schema buildSchema(List<ColumnDefinition> columns) {
        List<Types.NestedField> fields = new ArrayList<>();
        int id = 1;
        for (ColumnDefinition col : columns) {
            Type type = mapType(col.getType());
            Types.NestedField field = col.isRequired() ? Types.NestedField.required(id++, col.getName(), type) : Types.NestedField.optional(id++, col.getName(), type);
            fields.add(field);
        }
        return new Schema(fields);
    }

    private PartitionSpec buildPartitionSpec(Schema schema, List<PartitionDefinition> partitions) {
        PartitionSpec.Builder builder = PartitionSpec.builderFor(schema);
        if (partitions != null) {
            for (PartitionDefinition p : partitions) {
                String transform = p.getTransform().toLowerCase();
                String name = p.getName();
                switch (transform) {
                    case "identity":
                        builder.identity(name);
                        break;
                    case "year":
                        builder.year(name);
                        break;
                    case "month":
                        builder.month(name);
                        break;
                    case "day":
                        builder.day(name);
                        break;
                    case "hour":
                        builder.hour(name);
                        break;
                    case "void":
                    case "alwaysnull":
                        builder.alwaysNull(name);
                        break;
                    case "bucket":
                    case "truncate":
                        throw new UnsupportedOperationException("Unimplemented partition transform");
                    default:
                        throw new IllegalArgumentException("Unsupported partition transform: " + transform);
                }
            }
        }
        return builder.build();
    }


    private Type mapType(String type) {
        switch (type.toLowerCase()) {
            case "string":
                return Types.StringType.get();
            case "int":
                return Types.IntegerType.get();
            case "bigint":
            case "long":
                return Types.LongType.get();
            case "float":
                return Types.FloatType.get();
            case "double":
                return Types.DoubleType.get();
            case "boolean":
                return Types.BooleanType.get();
            case "timestamp":
                return Types.TimestampType.withoutZone();
            case "date":
                return Types.DateType.get();
            default:
                throw new IllegalArgumentException("Unsupported column type: " + type);
        }
    }

    private void addTransform(UpdatePartitionSpec update, String column, String transform) {
        switch (transform.toLowerCase()) {
            case "identity":
                update.addField(column);
                break;
            case "year":
                update.addField("year(" + column + ")");
                break;
            case "month":
                update.addField("month(" + column + ")");
                break;
            case "day":
                update.addField("day(" + column + ")");
                break;
            case "hour":
                update.addField("hour(" + column + ")");
                break;
            case "void":
            case "alwaysnull":
                update.addField("void(" + column + ")");
                break;
            case "bucket":
            case "truncate":
                throw new UnsupportedOperationException("Unimplemented partition transform");
            default:
                throw new IllegalArgumentException("Unsupported transform: " + transform);
        }
    }

}
