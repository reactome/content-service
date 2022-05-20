package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;


public class ReferenceEntityControllerTest extends BaseTest {

    @Test
    public void getReferenceEntitiesFor() throws Exception {
        mockMvcGetResult("/references/mapping/15357", "application/json;Charset=UTF-8");
    }

    @Test
    public void getCrossReferencesFor() throws Exception{
        mockMvcGetResult("/references/mapping/P36897/xrefs", "application/json;Charset=UTF-8");
    }

    @Test
    public void getCrossReferencesForList() throws Exception{

        Map<String, Object> params = new HashMap<>();
        params.put("page", 0);
        params.put("pageSize", 100);

        mockMvcPostResult("/references/mapping/xrefs","P36897, Q5S007", params);
    }
}