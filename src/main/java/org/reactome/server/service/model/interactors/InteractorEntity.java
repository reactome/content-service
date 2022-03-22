package org.reactome.server.service.model.interactors;

import io.swagger.v3.oas.annotations.media.Schema;
import org.reactome.server.graph.domain.model.Interaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps an Entity which is the accessionA and its interactions list.
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
public class InteractorEntity {

    @Schema(description = "This is the interactor accession (or identifier).")
    private String acc;

    @Schema(description = "This is the number of interactions for the given accession.")
    private Integer count;

    @Schema(description = "List of Interactors that interacts with the given accession.")
    private List<Interactor> interactors;

    public InteractorEntity() {
    }

    public InteractorEntity(String acc, List<Interaction> interactions) {
        this.acc = acc;
        this.count = interactions.size();
        this.interactors = new ArrayList<>();
        for (Interaction interaction : interactions) {
            interactors.add(new Interactor(interaction));
        }
    }

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
