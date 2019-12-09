package org.reactome.server.service.controller.graph;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class PathwaysControllerExampleTest  extends BaseTest {

    @Test
    public void getBean() {
      findBeanByName("pathwaysController");
    }

    @Test
    public void getContainedEvents() throws Exception {
        MvcResult mvcResult = this.getMockMvc().perform(
                get("/data/pathway/R-HSA-69620/containedEvents"))
                .andExpect(status().isOk())
                //  .andExpect(jsonPath("$", hasSize(2543)))
                //  .andExpect(jsonPath("$[0].stId").value("R-HSA-1475029"))
                //  .andExpect(jsonPath("$[?(@.stId=='R-HSA-1475029')]").exists())
                .andExpect(jsonPath("$[?(@.stId)].stId", Matchers.hasItems("R-HSA-141444")))
                //  .andExpect(jsonPath("$[?(@.stId)].length()", Matchers.hasSize(greaterThan(245))))
                //  .andExpect(jsonPath("$", Matchers.hasSize(greaterThan(245))))
                .andExpect(jsonPath("$").value(Matchers.hasSize(greaterThan(2))))
                .andReturn();

        //.andExpect(content().contentType("application/json;charset=UTF-8"))
        Assert.assertEquals("application/json;charset=UTF-8",
                mvcResult.getResponse().getContentType());

        MvcResult mvcResultNotFound = this.getMockMvc().perform(
                get("/data/pathway/r-hsa-69620/containedEvents"))
                .andExpect(status().isNotFound())
                .andReturn();

        Assert.assertEquals(404, mvcResultNotFound.getResponse().getStatus());

    }

    @Test
    public void getContainedEventsWithAttribute() throws Exception {
        MvcResult mvcResult = this.getMockMvc().perform(
                get("/data/pathway/R-HSA-69618/containedEvents/stId"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("R-HSA-141444")))
                .andReturn();

        Assert.assertEquals("text/plain;charset=ISO-8859-1",
                mvcResult.getResponse().getContentType());


        MvcResult mvcResultNotFound = this.getMockMvc().perform(
                get("/data/pathway/r-hsa-6968/containedEvents/displayname"))
                .andExpect(status().isNotFound())
                .andReturn();

    }

    @Test
    public void getTopLevelPathways() throws Exception {


        MvcResult mvcResult = this.getMockMvc().perform(
                get("/data/pathways/top/9606")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertEquals("application/json;charset=UTF-8",
                mvcResult.getResponse().getContentType());

    }

    @Test
    public void getPathwaysFor() throws Exception {
        MvcResult mvcResult = this.getMockMvc().perform(
                get("/data/pathways/low/entity/R-HSA-199420")
                        .param("species", "9606"))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertEquals("application/json;charset=UTF-8",
                mvcResult.getResponse().getContentType());
    }

    @Test
    public void getPathwaysForAllFormsOf() throws Exception {

        MvcResult mvcResult = this.getMockMvc().perform(get("/data/pathways/low/entity/R-HSA-199420/allForms")
                .contentType(MediaType.APPLICATION_JSON)
                .param("species","9606"))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertEquals("application/json;charset=UTF-8",
                mvcResult.getResponse().getContentType());

    }

    @Test
    public void getPathwaysWithDiagramFor() {
    }

    @Test
    public void getPathwaysWithDiagramForAllFormsOf() {
    }

    @Test
    public void getLowerLevelPathwaysForIdentifier() {
    }

    @Test
    public void getEntitiesInDiagramForIdentifier() {
    }

    @Test
    public void getCuratedTopLevelPathways() {
    }
}