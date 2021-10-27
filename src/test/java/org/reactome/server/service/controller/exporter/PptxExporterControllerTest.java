package org.reactome.server.service.controller.exporter;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;


public class PptxExporterControllerTest extends BaseTest {

    @Test
    public void diagramPPTX() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("profile", "Modern");
        params.put("flgInteractors", true);
        //pathway
        mockMvcGetResult("/exporter/diagram/R-HSA-68886.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation;charset=UTF-8", params);
        //reaction
        mockMvcGetResult("/exporter/reaction/R-HSA-6789031.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation;charset=UTF-8", params);
    }
}