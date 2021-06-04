package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class ReferenceEntityControllerTest extends BaseTest {

    @Test
    public void getReferenceEntitiesFor() throws Exception {
        mockMvcGetResult("/references/mapping/15357", "application/json;Charset=UTF-8");
    }
}