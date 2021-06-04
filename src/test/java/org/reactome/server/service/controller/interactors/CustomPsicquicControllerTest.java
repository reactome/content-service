package org.reactome.server.service.controller.interactors;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class CustomPsicquicControllerTest extends BaseTest {

    @Test
    public void registryPsicquicURL() throws Exception {
        String url = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query";

        String url2 = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query/species:human?firstResult=0&maxResults=10";

        mockMvcPostResult("/interactors/upload/psicquic/url", url, "name", "CSTest");
    }
}