package org.reactome.server.service.controller.graph;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class PathwaysControllerTest extends BaseTest {

    @Test
    public void getContainedEvents() throws Exception {

        MvcResult mvcResult = this.getMockMvc().perform(
                get("/data/pathway/R-HSA-69620/containedEvents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[?(@.stId)].stId", Matchers.hasItems("R-HSA-141444")))
                .andExpect(jsonPath("$").value(Matchers.hasSize(greaterThan(2))))
                .andReturn();

        mockMvcGetResultNotFound("/data/pathway/r-hsa-69620/containedEvents");
    }

    @Test
    public void getContainedEventsWithAttribute() throws Exception {

        MvcResult mvcResult = this.getMockMvc().perform(
                get("/data/pathway/R-HSA-69618/containedEvents/stId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string(containsString("R-HSA-141444")))
                .andReturn();

        mockMvcGetResultNotFound("/data/pathway/r-hsa-6968/containedEvents/displayname");
    }

    @Test
    public void getTopLevelPathways() throws Exception {

        mockMvcGetResult("/data/pathways/top/9606", "application/json;charset=UTF-8");

        mockMvcGetResultNotFound("/data/pathways/top/12345");
    }

    @Test
    public void getPathwaysFor() throws Exception {
        mockMvcGetResult("/data/pathways/top/9606", "application/json;charset=UTF-8", "species", "9606");
    }

    @Test
    public void getPathwaysForAllFormsOf() throws Exception {
        mockMvcGetResult("/data/pathways/top/9606", "application/json;charset=UTF-8", "species", "9606");
    }

    @Test
    public void getPathwaysWithDiagramFor() throws Exception {

        mockMvcGetResult("/data/pathways/low/diagram/entity/R-HSA-1640170", "application/json;charset=UTF-8", "species", "9606");

        mockMvcGetResultNotFound("/data/pathways/low/diagram/entity/R-HSA-5675194", "species", "9606");
    }

    @Test
    public void getPathwaysWithDiagramForAllFormsOf() throws Exception {

        mockMvcGetResult("/data/pathways/low/diagram/entity/R-HSA-199420/allForms", "application/json;charset=UTF-8", "species", "9606");

        mockMvcGetResultNotFound("/data/pathways/low/diagram/entity/R-HSA-5672972", "species", "9606");
    }

    //##################### API Ignored but still available for internal purposes #####################//
    @Test
    public void getLowerLevelPathwaysForIdentifier() throws Exception {
        mockMvcGetResult("/data/pathways/low/diagram/identifier/PTEN/allForms", "application/json;charset=UTF-8", "species", "9606");
    }

    @Test
    public void getEntitiesInDiagramForIdentifier() throws Exception {

        mockMvcGetResult("/data/diagram/R-HSA-4085001/entities/CTSA", "application/json;charset=UTF-8");
        //   mvcGetResult("/data/diagram/R-HSA-446203/entities/CTSA", "application/json;charset=UTF-8"); // pathway id is wrong
    }

    @Test
    public void getCuratedTopLevelPathways() throws Exception {
        mockMvcGetResult("/data/pathways/top/9606/curated", "application/json;charset=UTF-8");
    }
}