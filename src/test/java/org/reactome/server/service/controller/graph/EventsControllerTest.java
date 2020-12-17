package org.reactome.server.service.controller.graph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
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