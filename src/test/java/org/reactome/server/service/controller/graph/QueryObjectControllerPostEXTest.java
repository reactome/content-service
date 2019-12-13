package org.reactome.server.service.controller.graph;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class QueryObjectControllerPostEXTest extends BaseTest {

    @Test
    public void findById() {
    }

    @Test
    public void testFindById() {
    }

    @Test
    public void findByIds() throws Exception {
        MvcResult mvcResult = this.getMockMvc().perform(
                post("/data/query/ids")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content("R-HSA-141409, R-HSA-141431, R-HSA-141422, R-HSA-141439"))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertEquals("application/json;charset=UTF-8", mvcResult.getResponse().getContentType());

        MvcResult mvcResultNotFound = this.getMockMvc().perform(
                post("/data/query/ids")
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content("R-HSA-141409R-HSA-141422"))
                .andExpect(status().isNotFound())
                .andReturn();

        Assert.assertEquals(404, mvcResultNotFound.getResponse().getStatus());
    }

    @Test
    public void findByIdsMap() {
    }

    @Test
    public void findEnhancedObjectById() {
    }

    @Test
    public void findMoreObjectById() {
    }

    @Test
    public void findByIdNoRelations() {
    }

    @Test
    public void testFindByIdNoRelations() {
    }
}