package org.reactome.server.service.model.interactors;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Maps an Entity which is the accessionA and its interactions list.
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
public class InteractorEntity {

    @ApiModelProperty(value = "This is the interactor accession (or identifier).")
    private String acc;

    @ApiModelProperty(value = "This is the number of interactions for the given accession.")
    private Integer count;

    @ApiModelProperty(value = "List of Interactors that interacts with the given accession.")
    private List<Interactor> interactors;

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

    public List<Interactor> getInteractors() {
        return interactors;
    }

    public void setInteractors(List<Interactor> interactors) {
        this.interactors = interactors;
    }
}
