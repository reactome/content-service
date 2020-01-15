package org.reactome.server.service.model.interactors;

import io.swagger.annotations.ApiModelProperty;
import org.reactome.server.graph.domain.model.Interaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps an Interaction in the JSON output
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
public class Interactors {

    @ApiModelProperty(value = "This is the resource where interactors have been queried.")
    private String resource;

    @ApiModelProperty(value = "This is the list of entities which have been requested.")
    private List<InteractorEntity> entities = new ArrayList<>();

    public Interactors() { }

    public void add(String acc, List<Interaction> interactions){
        entities.add(new InteractorEntity(acc, interactions));
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public List<InteractorEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<InteractorEntity> entities) {
        this.entities = entities;
    }

    public boolean hasInteractorsInEntities() {
        int count = 0;
        for (InteractorEntity list : getEntities()) {
            count += list.getCount();
            if (count > 0) {
                break;
            }
        }
        return count == 0;
    }
}