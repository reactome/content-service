package org.reactome.server.service.controller.interactors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class CustomPsicquicControllerTest extends BaseTest {

    @Test
    public void registryPsicquicURL() throws Exception {
        String url = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query";

        mockMvcPostResult("/interactors/upload/psicquic/url", url, "name", "CSTest");
    }
}