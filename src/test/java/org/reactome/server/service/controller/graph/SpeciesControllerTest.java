package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class SpeciesControllerTest extends BaseTest {

    @Test
    public void getSpecies() throws Exception {
        mockMvcGetResult("/data/species/all", "application/json;charset=UTF-8");
    }

    @Test
    public void getAllSpecies() throws Exception {
        mockMvcGetResult("/data/species/all", "application/json;charset=UTF-8");
    }
}