package org.reactome.server.service.controller.graph.util;

import org.neo4j.driver.Value;

public class ShortCrossReference {

    private String identifier;
    private String databaseName;
    private String url;


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static ShortCrossReference build(Value value) {
        ShortCrossReference instance = new ShortCrossReference();
        instance.setDatabaseName(value.get("databaseName").asString());
        instance.setUrl(value.get("url").asString());
        instance.setIdentifier(value.get("identifier").asString());
        return instance;
    }
}
