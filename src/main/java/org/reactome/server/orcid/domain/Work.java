package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * POJO representing a Work in Orcid
 * It can be accessed in READ / WRITE operations
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@JsonTypeName("work")
public class Work implements Serializable {

    @JsonProperty("created-date")
    private Value createdDate;

    @JsonProperty("last-modified-date")
    private Value lastModifiedDate;

    @JsonProperty("source")
    private Source source;

    @JsonProperty("put-code")
    private Long putCode;

    @JsonProperty("path")
    private String path;

    @JsonProperty("title")
    private WorkTitle workTitle;

    @JsonProperty("short-description")
    private String shortDescription;

    @JsonProperty("publication-date")
    private PublicationDate publicationDate;

    @JsonProperty("citation")
    private Citation citation;

    @JsonProperty("journal-title")
    private Value journalTitle;

    @JsonProperty("type")
    private String type;

    @JsonProperty("external-ids")
    private ExternalIds externalIds;

    @JsonProperty("url")
    private Value url;

    @JsonProperty("contributors")
    private WorkContributors contributors;

    @JsonProperty("language-code")
    private String languageCode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty(value = "display-index", access = JsonProperty.Access.WRITE_ONLY)
    private String displayIndex;

    public Work() {
    }

    public Value getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Value createdDate) {
        this.createdDate = createdDate;
    }

    public Value getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Value lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Long getPutCode() {
        return putCode;
    }

    public void setPutCode(Long putCode) {
        this.putCode = putCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public WorkTitle getWorkTitle() {
        return workTitle;
    }

    public void setWorkTitle(WorkTitle workTitle) {
        this.workTitle = workTitle;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public PublicationDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(PublicationDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Citation getCitation() {
        return citation;
    }

    public void setCitation(Citation citation) {
        this.citation = citation;
    }

    public Value getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(Value journalTitle) {
        this.journalTitle = journalTitle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ExternalIds getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(ExternalIds externalIds) {
        this.externalIds = externalIds;
    }

    public void addExternalId(ExternalId externalId) {
        if (externalIds == null) {
            externalIds = new ExternalIds();
        }
        externalIds.getExternalId().add(externalId);
    }

    public Value getUrl() {
        return url;
    }

    public void setUrl(Value url) {
        this.url = url;
    }

    @JsonIgnore
    public void setUrl(String url) {
        setUrl(new Value(url));
    }

    public WorkContributors getContributors() {
        return contributors;
    }

    public void setContributors(WorkContributors contributors) {
        this.contributors = contributors;
    }

    public void addContributor(WorkContributor workContributor) {
        if (contributors == null) {
            contributors = new WorkContributors();
        }
        contributors.getContributors().add(workContributor);
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(String displayIndex) {
        this.displayIndex = displayIndex;
    }

    @Override
    public String toString() {
        return "Work{" +
                "createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", source=" + source +
                ", putCode=" + putCode +
                ", path='" + path + '\'' +
                ", workTitle=" + workTitle +
                ", shortDescription='" + shortDescription + '\'' +
                ", publicationDate='" + publicationDate + '\'' +
                ", citation=" + citation +
                ", journalTitle=" + journalTitle +
                ", type='" + type + '\'' +
                ", externalIds=" + externalIds +
                ", url=" + url +
                ", contributors=" + contributors +
                ", languageCode='" + languageCode + '\'' +
                ", country='" + country + '\'' +
                ", visibility='" + visibility + '\'' +
                ", displayIndex='" + displayIndex + '\'' +
                '}';
    }
}
