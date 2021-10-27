package org.reactome.server.service.controller.exporter;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;


public class EventPdfControllerTest extends BaseTest {

    @Test
    public void eventPdf() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("level", 1);
        params.put("diagramProfile", "Modern");
        params.put("resource", "total");
        params.put("expColumn", 1);
        params.put("analysisProfile", "Standard");

        mockMvcGetResult("/exporter/document/event/R-HSA-69620.pdf", "application/pdf;charset=UTF-8", params);
    }
}