package org.reactome.server.service.model.interactors;

import io.swagger.annotations.ApiModelProperty;
import org.reactome.server.graph.domain.model.*;

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
    private Long id = null;

    @ApiModelProperty(value = "This is the Interactions evidences.")
    private Integer evidences = null;

    @ApiModelProperty(value = "This represents the confidence value (score) of an interaction.")
    private Double score;

    @ApiModelProperty(value = "This represents the URL for given accession.")
    private String accURL;

    @ApiModelProperty(value = "This represents the URL for the given interactions identifiers.")
    private String evidencesURL;

    public Interactor() {}

    public Interactor(Interaction interaction) {
        this.id = interaction.getDbId();

        ReferenceEntity re;
        if(interaction instanceof UndirectedInteraction){
            re = ((UndirectedInteraction) interaction).getInteractor().get(0);
        } else {
            re = ((DirectedInteraction) interaction).getTarget();
        }

        if (re instanceof ReferenceIsoform) {
            String vi = ((ReferenceIsoform) re).getVariantIdentifier();
            this.acc = (vi != null && !vi.isEmpty()) ? vi : re.getIdentifier();
        } else {
            this.acc =  re.getIdentifier();
        }
        this.accURL = re.getUrl();

        this.evidences = interaction.getAccession().size();
        this.evidencesURL = interaction.getUrl();

        this.score = interaction.getScore();

        if(re instanceof ReferenceSequence){
            ReferenceSequence rs = (ReferenceSequence) re;
            if (rs.getGeneName() != null && !rs.getGeneName().isEmpty()) this.alias = rs.getGeneName().get(0);
        }
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
