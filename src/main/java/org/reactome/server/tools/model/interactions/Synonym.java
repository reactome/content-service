package org.reactome.server.tools.model.interactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Synonym {

    @JsonIgnore
    private String acc;
    private String text;
    private String imageUrl;

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
