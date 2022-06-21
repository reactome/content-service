package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;


public class EventsControllerTest extends BaseTest {

    @Test
    public void getEventAncestors() throws Exception {
        //pathway
        mockMvcGetResult("/data/event/9607240/ancestors", "application/json;Charset=UTF-8");
        //reaction
        mockMvcGetResult("/data/event/69173/ancestors", "application/json;Charset=UTF-8");
    }

    @Test
    public void getEventHierarchy() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("pathwaysOnly", false);
        params.put("resource", "TOTAL");
        params.put("interactors", false);
        mockMvcGetResult("/data/eventsHierarchy/9913", "application/json;Charset=UTF-8", params);
    }
}