package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class GeneralControllerTest extends BaseTest {

    @Test
    public void getDBName() throws Exception {
        this.getMockMvc().perform(get("/data/database/name"))
                .andExpect(status().isOk())
                .andExpect(content().string("reactome"))
                .andReturn();
    }

    @Test
    public void getDBVersion() throws Exception {
        mockMvcGetResult("/data/database/version", "text/plain;charset=UTF-8");
    }
}