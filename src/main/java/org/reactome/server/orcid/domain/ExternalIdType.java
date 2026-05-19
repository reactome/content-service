package org.reactome.server.orcid.domain;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public enum ExternalIdType {
    DOI("doi"),
    OTHERID("other-id");
    private String name;
    ExternalIdType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
