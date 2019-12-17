package org.reactome.server.service.controller.interactors;

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
public class CustomPsicquicControllerTest extends BaseTest {

    @Test
    public void registryPsicquicURL() throws Exception {

        // todo internal server error ->Gui fix it
        //String urlWrong = "http://mentha.uniroma2.it:9090/psicquic/webservices/current/search/query/*?firstResult=10000&maxResults=50";

        //String urlWrong2 = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query/Q9UBU9";

        String url = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/query";

        mvcPostResult("/interactors/upload/psicquic/url", url, "name", "CSTest");
    }
}