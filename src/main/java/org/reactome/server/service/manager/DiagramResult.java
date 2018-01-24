package org.reactome.server.service.manager;

import java.util.List;

public class DiagramResult {

    private String diagramStId;

    private List<String> events;

    private Integer size;

    public String getDiagramStId() {
        return diagramStId;
    }

    public List<String> getEvents() {
        return events;
    }

    public Integer getSize() {
        return size;
    }
}
