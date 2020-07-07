package org.reactome.server.service.controller.interactors;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class PsicquicInteractionsControllerTest extends BaseTest {

    @Test
    public void getResources() throws Exception {
        mockMvcGetResult("/interactors/psicquic/resources", "application/json;charset=UTF-8");
    }

    @Test
    public void getProteinDetailsByResource() throws Exception {
        mockMvcGetResult("/interactors/psicquic/molecule/mint/Q13501/details", "application/json;charset=UTF-8");
    }

    @Test
    public void getProteinsDetailsByResource() throws Exception {
        mockMvcPostResult("/interactors/psicquic/molecules/uniprot/details", "P11142, P3030");
    }

    @Test
    public void getProteinSummaryByResource() throws Exception {
        this.getMockMvc().perform(
                get("/interactors/psicquic/molecule/MINT/P30304/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.entities[0].count", Matchers.greaterThan(5)))  //6
                .andReturn();
    }

    @Test
    public void getProteinsSummaryByResource() throws Exception {
        this.getMockMvc().perform(
                post("/interactors/psicquic/molecules/intact/summary")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Q00987, P30307"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.entities[0].count", Matchers.greaterThan(25))) // 38
                .andReturn();
    }
}