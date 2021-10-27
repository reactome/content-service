package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;



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
        mockMvcGetResult("/data/eventsHierarchy/9913", "application/json;Charset=UTF-8");
    }
}