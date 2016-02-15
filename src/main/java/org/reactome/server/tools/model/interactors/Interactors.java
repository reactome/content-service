package org.reactome.server.tools.model.interactors;

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

    /**
     * (e.g http://identifier.org/chebi/CHEBI:##ID##)
     */
    @ApiModelProperty(value = "This is the URL for the Chemicals.")
    private String chemicalURL = "http://identifiers.org/chebi/##ID##";

    /**
     *  (e.g. http://identifier.org/uniprot/##ID##)
     */
    @ApiModelProperty(value = "This is the URL for Proteins.")
    private String proteinURL = "http://identifiers.org/uniprot/##ID##";

    /**
     * (e.g. http://www.ebi.ac.uk/intact/pages/interactions/interactions.xhtml?query=##ID##)
     */
    @ApiModelProperty(value = "This is the interaction URL.")
    private String interactionURL = "http://www.ebi.ac.uk/intact/pages/interactions/interactions.xhtml?query=##ID##";

    @ApiModelProperty(value = "This is the list of entities which have been requested.")
    private List<InteractorEntity> entities;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getChemicalURL() {
        return chemicalURL;
    }

    public void setChemicalURL(String chemicalURL) {
        this.chemicalURL = chemicalURL;
    }

    public String getProteinURL() {
        return proteinURL;
    }

    public void setProteinURL(String proteinURL) {
        this.proteinURL = proteinURL;
    }

    public String getInteractionURL() {
        return interactionURL;
    }

    public void setInteractionURL(String interactionURL) {
        this.interactionURL = interactionURL;
    }

    public List<InteractorEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<InteractorEntity> entities) {
        this.entities = entities;
    }

}