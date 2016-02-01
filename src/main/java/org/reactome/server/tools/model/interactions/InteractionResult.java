package org.reactome.server.tools.model.interactions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Deprecated
public class InteractionResult {

    private String resource;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String interactorUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String interactionUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Entity> entities;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Synonym> synonym;


    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getInteractorUrl() {
        return interactorUrl;
    }

    public void setInteractorUrl(String interactorUrl) {
        this.interactorUrl = interactorUrl;
    }

    public String getInteractionUrl() {
        return interactionUrl;
    }

    public void setInteractionUrl(String interactionUrl) {
        this.interactionUrl = interactionUrl;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public Map<String, Synonym> getSynonym() {
        return synonym;
    }

    public void setSynonym(Map<String, Synonym> synonym) {
        this.synonym = synonym;
    }

}
