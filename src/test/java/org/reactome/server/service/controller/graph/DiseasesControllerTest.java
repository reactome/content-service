package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class DiseasesControllerTest extends BaseTest {

    @Test
    public void getDiseases() throws Exception {
        mockMvcGetResult("/data/diseases", "application/json;charset=UTF-8");
    }

    @Test
    public void getDiseasesSummary() throws Exception {
        mockMvcGetResult("/data/diseases/doid", "text/plain;charset=UTF-8");
    }
}