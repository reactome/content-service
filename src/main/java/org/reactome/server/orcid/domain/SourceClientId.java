package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class SourceClientId implements Serializable {

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("path")
    private String path;

    @JsonProperty("host")
    private String host;

    public SourceClientId() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "SourceClientId{" +
                "uri='" + uri + '\'' +
                ", path='" + path + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
