package org.reactome.server.service.model.content;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Schema class attribute/property")
public class AttributeResponse {

    @Schema(description = "Property name", example = "displayName")
    private String name;

    @Schema(description = "Cardinality: '1' for single, '+' for multi-value", example = "1")
    private String cardinality;

    @Schema(description = "Possible value types for this property")
    private List<ValueType> valueTypes;

    @Schema(description = "Class where this property is declared", example = "DatabaseObject")
    private String origin;

    public AttributeResponse() {}

    public AttributeResponse(String name, String cardinality, List<ValueType> valueTypes, String origin) {
        this.name = name;
        this.cardinality = cardinality;
        this.valueTypes = valueTypes;
        this.origin = origin;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCardinality() { return cardinality; }
    public void setCardinality(String cardinality) { this.cardinality = cardinality; }

    public List<ValueType> getValueTypes() { return valueTypes; }
    public void setValueTypes(List<ValueType> valueTypes) { this.valueTypes = valueTypes; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    @Schema(description = "A value type for an attribute")
    public static class ValueType {
        @Schema(description = "Type name", example = "String")
        private String name;

        @Schema(description = "Whether this type is a Reactome DatabaseObject")
        private boolean databaseObject;

        public ValueType() {}

        public ValueType(String name, boolean databaseObject) {
            this.name = name;
            this.databaseObject = databaseObject;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isDatabaseObject() { return databaseObject; }
        public void setDatabaseObject(boolean databaseObject) { this.databaseObject = databaseObject; }
    }
}
