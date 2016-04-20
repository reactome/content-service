package org.reactome.server.service.model.interactors;

import io.swagger.annotations.ApiModelProperty;

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
    private List<InteractorEntity> entities;

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

}