package org.reactome.server.service.model.content;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DOI pathway entry")
public class DoiPathwayResponse {

    @Schema(description = "Stable identifier")
    private String stId;

    @Schema(description = "Pathway display name")
    private String displayName;

    @Schema(description = "Digital Object Identifier")
    private String doi;

    @Schema(description = "Species name")
    private String species;

    @Schema(description = "Initial release date")
    private String releaseDate;

    @Schema(description = "Last revision date")
    private String reviseDate;

    @Schema(description = "Release status: NEW, UPDATED, or empty")
    private String releaseStatus;

    @Schema(description = "Authors")
    private List<SimplePerson> authors;

    @Schema(description = "Reviewers")
    private List<SimplePerson> reviewers;

    @Schema(description = "Editors")
    private List<SimplePerson> editors;

    public DoiPathwayResponse() {}

    public String getStId() { return stId; }
    public void setStId(String stId) { this.stId = stId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getReviseDate() { return reviseDate; }
    public void setReviseDate(String reviseDate) { this.reviseDate = reviseDate; }

    public String getReleaseStatus() { return releaseStatus; }
    public void setReleaseStatus(String releaseStatus) { this.releaseStatus = releaseStatus; }

    public List<SimplePerson> getAuthors() { return authors; }
    public void setAuthors(List<SimplePerson> authors) { this.authors = authors; }

    public List<SimplePerson> getReviewers() { return reviewers; }
    public void setReviewers(List<SimplePerson> reviewers) { this.reviewers = reviewers; }

    public List<SimplePerson> getEditors() { return editors; }
    public void setEditors(List<SimplePerson> editors) { this.editors = editors; }
}
