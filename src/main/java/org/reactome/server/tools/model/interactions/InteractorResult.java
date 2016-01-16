package org.reactome.server.tools.model.interactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class InteractorResult {

    private String acc;
    @JsonProperty("id")
    private String interactionId;
    private Double score;

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

}
