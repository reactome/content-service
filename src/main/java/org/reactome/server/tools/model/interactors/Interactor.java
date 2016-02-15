package org.reactome.server.tools.model.interactors;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Interaction ID
     **/
    @ApiModelProperty(value = "This is the interaction ID of two accessions.")
    private String id = null;

    @ApiModelProperty(value = "This is the clustered Interactions identifiers.")
    private List<String> cluster = null;

    @ApiModelProperty(value = "This represents the confidence value of an interaction.")
    private Double score;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if(id != null && !id.isEmpty()) {
            this.id = id;
        }
    }

    public List<String> getCluster() {
        return cluster;
    }

    public void setCluster(List<String> cluster) {
        this.cluster = cluster;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void addCluster(String clusterIdentifier) {
        if(clusterIdentifier != null && !clusterIdentifier.isEmpty()) {
            if (cluster == null) {
                cluster = new ArrayList<>();
            }

            cluster.add(clusterIdentifier);
        }
    }

}
