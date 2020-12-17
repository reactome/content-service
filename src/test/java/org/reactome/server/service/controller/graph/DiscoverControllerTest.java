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
public class DiscoverControllerTest extends BaseTest {

    @Test
    public void eventDiscovery() throws Exception {
        //pathway
        mockMvcGetResult("/data/discover/5693532", "application/json;charset=UTF-8");
        //reaction
        mockMvcGetResult("/data/discover/5693977", "application/json;charset=UTF-8");
    }
}