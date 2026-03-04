package org.reactome.server.service.model.content;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight person representation")
public class SimplePerson {

    @Schema(description = "Database identifier")
    private Long dbId;

    @Schema(description = "Full display name")
    private String displayName;

    @Schema(description = "Surname")
    private String surname;

    @Schema(description = "First name")
    private String firstname;

    @Schema(description = "ORCID identifier")
    private String orcidId;

    public SimplePerson() {}

    public SimplePerson(Long dbId, String displayName, String surname, String firstname, String orcidId) {
        this.dbId = dbId;
        this.displayName = displayName;
        this.surname = surname;
        this.firstname = firstname;
        this.orcidId = orcidId;
    }

    public Long getDbId() { return dbId; }
    public void setDbId(Long dbId) { this.dbId = dbId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getOrcidId() { return orcidId; }
    public void setOrcidId(String orcidId) { this.orcidId = orcidId; }
}
