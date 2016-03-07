package org.reactome.server.tools.model.interactors;

import io.swagger.annotations.ApiModelProperty;

/**
 * Maps an Interactor and the Interaction Id
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
public class Interactor {

    @ApiModelProperty(value = "This is the interactor accession (or identifier).")
    private String acc;

    @ApiModelProperty(value = "This is the Gene name of the given accession.")
    private String alias;

    @ApiModelProperty(value = "This is an auto increment counter which represents an unique number for the interaction.")
    private Integer id = null;

    @ApiModelProperty(value = "This is the Interactions evidences.")
    private Integer evidences = null;

    @ApiModelProperty(value = "This represents the confidence value (score) of an interaction.")
    private Double score;

    @ApiModelProperty(value = "This represents the URL for given accession.")
    private String accURL;

    @ApiModelProperty(value = "This represents the URL for the given interactions identifiers.")
    private String evidencesURL;

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEvidences() {
        return evidences;
    }

    public void setEvidences(Integer evidences) {
        this.evidences = evidences;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getAccURL() {
        return accURL;
    }

    public void setAccURL(String accURL) {
        this.accURL = accURL;
    }

    public String getEvidencesURL() {
        return evidencesURL;
    }

    public void setEvidencesURL(String evidencesURL) {
        this.evidencesURL = evidencesURL;
    }
}
