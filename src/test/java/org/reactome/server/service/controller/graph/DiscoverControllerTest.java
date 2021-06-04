package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

public class DiscoverControllerTest extends BaseTest {

    @Test
    public void eventDiscovery() throws Exception {
        //pathway
        mockMvcGetResult("/data/discover/5693532", "application/json;charset=UTF-8");
        //reaction
        mockMvcGetResult("/data/discover/5693977", "application/json;charset=UTF-8");
    }
}