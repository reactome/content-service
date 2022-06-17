package org.reactome.server.service.model.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.result.model.EntityStatistics;
import org.reactome.server.analysis.core.result.model.ReactionStatistics;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;

import java.util.Map;
import java.util.stream.Collectors;

@JsonPropertyOrder({"stId", "name", "species", "type", "diagram", "entities", "reactions", "children"})
public class AnalysedPathwayBrowserNode extends PathwayBrowserNode {


    protected EntityStatistics entities;
    protected ReactionStatistics reactions;

    public AnalysedPathwayBrowserNode(PathwayBrowserNode node) {
        this.setStId(node.getStId());
        this.setName(node.getName());
        this.setSpecies(node.getSpecies());
        this.setUrl(node.getUrl());
        this.setType(node.getType());
        this.setDiagram(node.getDiagram());
        this.setUnique(node.getUnique());
        this.setOrder(node.getOrder());
        this.setHighlighted(node.getHighlighted());
        this.setClickable(node.isClickable());
        this.setChildren(node.getChildren());
        this.setParent(node.getParent());
    }

    public EntityStatistics getEntities() {
        return entities;
    }

    public ReactionStatistics getReactions() {
        return reactions;
    }

    public void initAnalysis(Map<String, PathwayNodeData> stIdToData, String resource, boolean includeInteractors) {

        PathwayNodeData analysisData = stIdToData.get(getStId());
        if (analysisData != null) {
            if (resource.equals("TOTAL")) {
                this.entities = new EntityStatistics(analysisData, includeInteractors);
                this.reactions = new ReactionStatistics(analysisData);
            } else {
                for (MainResource mr : analysisData.getResources()) {
                    if (mr.getName().equals(resource)) {
                        this.entities = new EntityStatistics(mr, analysisData, includeInteractors);
                        this.reactions = new ReactionStatistics(mr, analysisData);
                        break;
                    }
                }
            }
        }

        if (this.getChildren() != null) {
            this.setChildren(this.getChildren().stream()
                    .map(AnalysedPathwayBrowserNode::new)
                    .peek(node -> node.initAnalysis(stIdToData, resource, includeInteractors))
                    .collect(Collectors.toSet())
            );
        }
    }
}
