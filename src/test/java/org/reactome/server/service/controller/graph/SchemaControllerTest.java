package org.reactome.server.service.controller.graph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class SchemaControllerTest extends BaseTest {

    @Test
    public void getDatabaseObjectsForClassName() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("species", 9606);
        params.put("page", 1);
        params.put("offset", 20);

        mockMvcGetResult("/data/schema/TopLevelPathway", "Application/json;Charset=UTF-8", params);
    }

    @Test
    public void getSimpleDatabaseObjectByClassName() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("species", 9606);
        params.put("page", 1);
        params.put("offset", 20);

        mockMvcGetResult("/data/schema/TopLevelPathway/min", "Application/json;Charset=UTF-8", params);
    }

    @Test
    public void getSimpleReferencesObjectsByClassName() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("offset", 200);

        mockMvcGetResult("/data/schema/ReferenceGroup/reference", "Application/json;Charset=UTF-8", params);
    }

    @Test
    public void countEntries() throws Exception {
        mockMvcGetResult("/data/schema/TopLevelPathway/count", "Application/json;Charset=UTF-8", "species", "9606");
    }
}