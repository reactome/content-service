package org.reactome.server.service.controller.graph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class MappingControllerTest extends BaseTest {

    @Test
    public void getReactionsLikeEvent() throws Exception {
        mockMvcGetResult("/data/mapping/ENSEMBL/NTN1/reactions", "application/json;Charset=UTF-8");
        //pten should be PTEN, same as mapping pathways,lower case will cause 404
        mockMvcGetResultNotFound("/data/mapping/UniProt/pten/reactions", "species", "9606");
    }

    @Test
    public void getPathways() throws Exception {
        mockMvcGetResult("/data/mapping/UniProt/PTEN/pathways", "application/json;Charset=UTF-8");
    }
}