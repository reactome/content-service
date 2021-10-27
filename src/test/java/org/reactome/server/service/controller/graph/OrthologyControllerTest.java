package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class OrthologyControllerTest extends BaseTest {

    @Test
    public void getOrthology() throws Exception {
        mockMvcGetResult("/data/orthology/8956320/species/48898", "application/json;Charset=UTF-8");
    }

    @Test
    public void getOrthologies() throws Exception {
        mockMvcPostResult("/data/orthologies/ids/species/49633", "R-HSA-6799198,R-HSA-446203, R-HSA-4086398");
    }
}