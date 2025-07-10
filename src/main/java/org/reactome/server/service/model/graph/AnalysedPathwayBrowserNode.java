package org.reactome.server.service.model.graph;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.result.PathwayNodeSummary;
import org.reactome.server.analysis.core.result.model.EntityStatistics;
import org.reactome.server.analysis.core.result.model.ReactionStatistics;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;

import java.util.Map;
import java.util.stream.Collectors;

@JsonPropertyOrder({"stId", "name", "species", "type", "diagram", "llp", "totalEntity", "totalEntityAndInteractors", "entities", "reactions", "children"})
public class AnalysedPathwayBrowserNode extends SizedPathwayBrowserNode {

    protected Boolean llp;
    protected EntityStatistics entities;
    protected ReactionStatistics reactions;

    public AnalysedPathwayBrowserNode(SizedPathwayBrowserNode node) {
        super(node);
        this.setTotalEntity(node.getTotalEntity());
        this.setTotalEntityAndInteractors(node.getTotalEntityAndInteractors());
    }

    public Boolean isLlp() {
        return llp;
    }

    public EntityStatistics getEntities() {
        return entities;
    }

    public ReactionStatistics getReactions() {
        return reactions;
    }

    public void initAnalysis(Map<String, PathwayNodeSummary> stIdToData, String resource, boolean includeInteractors, boolean importableOnly) {

        PathwayNodeSummary summary = stIdToData.get(getStId());
        if (summary == null) return;
        this.llp = summary.isLlp();
        PathwayNodeData analysisData = summary.getData();
        if (analysisData == null) return;

        if (resource.equals("TOTAL")) {
            this.entities = new EntityStatistics(analysisData, includeInteractors, importableOnly);
            this.reactions = new ReactionStatistics(analysisData, importableOnly);
        } else {
            for (MainResource mr : analysisData.getResources()) {
                if (mr.getName().equals(resource)) {
                    this.entities = new EntityStatistics(mr, analysisData, includeInteractors);
                    this.reactions = new ReactionStatistics(mr, analysisData);
                    break;
                }
            }

        }

        if (this.getChildren() != null) {
            this.setChildren(this.getChildren().stream()
                    .map(node -> (SizedPathwayBrowserNode) node)
                    .map(AnalysedPathwayBrowserNode::new)
                    .peek(node -> node.initAnalysis(stIdToData, resource, includeInteractors, importableOnly))
                    .collect(Collectors.toSet())
            );
        }
    }
}
