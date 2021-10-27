package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


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