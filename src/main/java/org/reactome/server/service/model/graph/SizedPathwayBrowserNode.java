package org.reactome.server.service.model.graph;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.reactome.server.analysis.core.model.PathwayNode;
import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.graph.service.helper.PathwayBrowserNode;

import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@JsonPropertyOrder({"stId", "name", "species", "type", "diagram", "llp", "totalEntity", "totalEntityAndInteractors", "children"})
public class SizedPathwayBrowserNode extends PathwayBrowserNode {
    public SizedPathwayBrowserNode(PathwayBrowserNode source) {
        this.setStId(source.getStId());
        this.setName(source.getName());
        this.setSpecies(source.getSpecies());
        this.setUrl(source.getUrl());
        this.setType(source.getType());
        this.setDiagram(source.getDiagram());
        this.setUnique(source.getUnique());
        this.setOrder(source.getOrder());
        this.setHighlighted(source.getHighlighted());
        this.setClickable(source.isClickable());
        this.setChildren(source.getChildren());
        this.setParent(source.getParent());
    }

    public SizedPathwayBrowserNode(PathwayNode source) {
        this.setStId(source.getStId());
        this.setName(source.getName());
        this.setSpecies(source.getSpecies().getName());
        this.setType(source.getType());
        this.setDiagram(source.getDiagram() == source);
        this.setOrder(source.getOrder());
        this.setupNodeData(source.getPathwayNodeData());
        this.setChildren(source.getChildren().stream()
                .map(SizedPathwayBrowserNode::new)
                .collect(Collectors.toCollection(TreeSet::new))
        );
    }

    public void initSize(Map<String, PathwayNode> stIdToNode) {
        PathwayNode pathwayNode = stIdToNode.get(this.getStId());
        if (pathwayNode != null) setupNodeData(pathwayNode.getPathwayNodeData());

        if (this.getChildren() != null) {
            this.setChildren(this.getChildren().stream()
                    .map(SizedPathwayBrowserNode::new)
                    .peek(node -> node.initSize(stIdToNode))
                    .collect(Collectors.toCollection(TreeSet::new))
            );
        }
    }

    private void setupNodeData(PathwayNodeData nodeData) {
        this.totalEntity = nodeData.getEntitiesCount();
        this.totalEntityAndInteractors = nodeData.getEntitiesAndInteractorsCount();
    }

    protected int totalEntity;
    protected int totalEntityAndInteractors;

    public int getTotalEntity() {
        return totalEntity;
    }

    public void setTotalEntity(int totalEntity) {
        this.totalEntity = totalEntity;
    }

    public int getTotalEntityAndInteractors() {
        return totalEntityAndInteractors;
    }

    public void setTotalEntityAndInteractors(int totalEntityAndInteractors) {
        this.totalEntityAndInteractors = totalEntityAndInteractors;
    }

}
