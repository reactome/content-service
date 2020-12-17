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
public class DiseasesControllerTest extends BaseTest {

    @Test
    public void getDiseases() throws Exception {
        mockMvcGetResult("/data/diseases", "application/json;charset=UTF-8");
    }

    @Test
    public void getDiseasesSummary() throws Exception {
        mockMvcGetResult("/data/diseases/doid", "text/plain;charset=ISO-8859-1");
    }
}