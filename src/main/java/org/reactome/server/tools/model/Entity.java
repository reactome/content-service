package org.reactome.server.tools.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {

    private String acc;
    private Integer count;
    private List<InteractorResult> interactors;

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<InteractorResult> getInteractors() {
        return interactors;
    }

    public void setInteractors(List<InteractorResult> interactors) {
        this.interactors = interactors;
    }
}
