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
public class PhysicalEntityControllerTest extends BaseTest {

    @Test
    public void getOtherFormsOf() throws Exception {

        //stId
        mockMvcGetResult("/data/entity/R-HSA-202772/otherForms", "application/json;Charset=UTF-8");
        //dbId
        mockMvcGetResult("/data/entity/159865/otherForms", "application/json;Charset=UTF-8");
    }

    @Test
    public void getComponentsOf() throws Exception {
        mockMvcGetResult("/data/entity/203972/componentOf", "application/json;Charset=UTF-8");
    }

    @Test
    public void getComplexSubunits() throws Exception {
        mockMvcGetResult("/data/complex/R-HSA-1252247/subunits", "application/json;Charset=UTF-8", "excludeStructures", "false");
    }

    @Test
    public void getComplexesFor() throws Exception {
        mockMvcGetResult("/data/complexes/UniProt/P00747", "application/json;Charset=UTF-8");
    }

    //##################### API Ignored but still available for internal purposes #####################//
    @Test
    public void getReferenceMolecules() throws Exception {
        mockMvcGetResult("/data/referenceMolecules", "application/json;Charset=UTF-8");
    }

    @Test
    public void getReferenceMoleculesSummary() throws Exception {
        mockMvcGetResult("/data/referenceMolecules/identifiers", "text/plain;charset=ISO-8859-1");
    }

    @Test
    public void getReferenceSequences() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("offset", 20);
        mockMvcGetResult("/data/referenceSequences", "application/json;Charset=UTF-8", params);
    }

    @Test
    public void getReferenceSequencesSummary() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("offset", 20);
        mockMvcGetResult("/data/referenceSequences/identifiers", "text/plain;charset=ISO-8859-1", params);
    }
}