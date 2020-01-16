package org.reactome.server.service.controller.graph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class SchemaControllerTest extends BaseTest {

    @Test
    public void getDatabaseObjectsForClassName() throws Exception {

        Map<String, Object> parmas = new HashMap<>();
        parmas.put("species", 9606);
        parmas.put("page", 1);
        parmas.put("offset", 20);

        mockMvcGetResult("/data/schema/TopLevelPathway", "Application/json;Charset=UTF-8", parmas);
    }

    @Test
    public void getSimpleDatabaseObjectByClassName() throws Exception {

        Map<String, Object> parmas = new HashMap<>();
        parmas.put("species", 9606);
        parmas.put("page", 1);
        parmas.put("offset", 20);

        mockMvcGetResult("/data/schema/TopLevelPathway/min", "Application/json;Charset=UTF-8", parmas);
    }

    @Test
    public void getSimpleReferencesObjectsByClassName() throws Exception {

        Map<String, Object> parmas = new HashMap<>();
        parmas.put("page", 1);
        parmas.put("offset", 200);

        mockMvcGetResult("/data/schema/ReferenceGroup/reference", "Application/json;Charset=UTF-8", parmas);
    }

    @Test
    public void countEntries() throws Exception {
        mockMvcGetResult("/data/schema/TopLevelPathway/count", "Application/json;Charset=UTF-8", "species", "9606");
    }
}