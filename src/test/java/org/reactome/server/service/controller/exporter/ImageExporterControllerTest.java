package org.reactome.server.service.controller.exporter;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;


public class ImageExporterControllerTest extends BaseTest {

    @Test
    public void diagramImage() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("quality", 5);
        //get value from solr
        params.put("flg", "UNC5B");
        params.put("flgInteractors", true);
        params.put("title", false);
        params.put("diagramProfile", "Modern");
        params.put("resource", "total");
        //pathway
        mockMvcGetResult("/exporter/diagram/166520.png", "image/png;charset=UTF-8", params);
        //reaction
        mockMvcGetResult("/exporter/diagram/6789031.png", "image/png;charset=UTF-8", params);
    }

    @Test
    public void reactionImage() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("quality", 5);
        params.put("flgInteractors", true);
        params.put("resource", "total");

        mockMvcGetResult("/exporter/reaction/70272.jpg", "image/jpg;;charset=UTF-8", params);
    }
}