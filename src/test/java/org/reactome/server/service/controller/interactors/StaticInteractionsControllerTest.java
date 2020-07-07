package org.reactome.server.service.controller.interactors;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class StaticInteractionsControllerTest extends BaseTest {

    @Test
    public void getProteinSummaryByAcc() throws Exception {
        mockMvcGetResult("/interactors/static/molecule/P16885/summary", "application/json;charset=UTF-8");
    }

    @Test
    public void getProteinDetailsByAcc() throws Exception {
        this.getMockMvc().perform(
                get("/interactors/static/molecule/Q13501/details")
                        .param("page", "-1")
                        .param("pageSize", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.entities[0].count", Matchers.greaterThan(15))) //20
                .andReturn();

        this.getMockMvc().perform(
                get("/interactors/static/molecule/q13501/details")
                        .param("page", "-1")
                        .param("pageSize", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andReturn();
    }

    @Test
    public void getProteinsSummaryByAccs() throws Exception {
        this.getMockMvc().perform(
                post("/interactors/static/molecules/summary")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("P23025"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.entities[0].count", Matchers.greaterThan(15)))  //35
                .andReturn();
    }

    @Test
    public void getProteinsDetailsByAccs() throws Exception {
        this.getMockMvc().perform(
                post("/interactors/static/molecules/details")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Q13501,P11142")
                        .param("page", "-1")
                        .param("pageSize", "-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.entities[0].count", Matchers.greaterThan(30)))  //35
                .andExpect(jsonPath("$.entities[1].count", Matchers.greaterThan(40)))  //68
                .andReturn();

        this.getMockMvc().perform(
                post("/interactors/static/molecules/details")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("p23025,O95632,O95634,O95635,O95636,O95637,O95638")
                        .param("page", "-1")
                        .param("pageSize", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andReturn();
    }

    @Test
    public void getLowerLevelPathways() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("species", "Homo sapiens");
        params.put("onlyDiagrammed", false);

        mockMvcGetResult("/interactors/static/molecule/Q9BXM7-1/pathways", "application/json;charset=UTF-8", params);
    }
}