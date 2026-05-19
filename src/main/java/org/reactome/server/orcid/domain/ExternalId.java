package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class ExternalId implements Serializable {

    @JsonProperty("external-id-type")
    private String type;

    @JsonProperty("external-id-value")
    private String value;

    @JsonProperty("external-id-url")
    private Value url;

    @JsonProperty("external-id-relationship")
    private String relationship = "SELF";

    public ExternalId() {}

    public ExternalId(String type, String value, String url, String relationship) {
        this.type = type;
        this.value = value;
        this.url = new Value(url);
        this.relationship = relationship;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Value getUrl() {
        return url;
    }

    @JsonIgnore
    public String getUrlString() {
        return url.getContent();
    }

    public void setUrl(Value url) {
        this.url = url;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    @Override
    public String toString() {
        return "ExternalId{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", url=" + url +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
