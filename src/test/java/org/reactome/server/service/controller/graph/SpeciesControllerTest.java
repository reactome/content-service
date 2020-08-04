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