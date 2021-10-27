package org.reactome.server.service.controller.exporter;

import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;


public class FireworksImageExporterControllerTest extends BaseTest {

    @Test
    public void diagramImage() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("quality", "5");
        params.put("flg", "PTEN");
        params.put("title", true);
        params.put("margin", 10);
        params.put("diagramProfile", "Copper");
        params.put("resource", "total");
        params.put("expColumn", "2");
        params.put("coverage", false);

        mockMvcGetResult("/exporter/fireworks/9606.svg", "image/svg+xml;charset=UTF-8;", params);
        mockMvcGetResult("/exporter/fireworks/9606.png", "image/png;charset=UTF-8;", params);
    }
}