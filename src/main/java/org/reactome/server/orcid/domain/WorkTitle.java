package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@JsonRootName("title")
public class WorkTitle implements Serializable {
    @JsonProperty("title")
    private Value title;

    @JsonProperty("subtitle")
    private String subtitle;

    @JsonProperty("translated-title")
    private String translatedTitle;

    public WorkTitle() {
    }

    public WorkTitle(Value title) {
        this.title = title;
    }

    public WorkTitle(String title) {
        this.title = new Value(title);
    }

    public Value getTitle() {
        return title;
    }

    public void setTitle(Value title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    @Override
    public String toString() {
        return "WorkTitle{" +
                "title=" + title +
                ", subtitle='" + subtitle + '\'' +
                ", translatedTitle='" + translatedTitle + '\'' +
                '}';
    }
}
