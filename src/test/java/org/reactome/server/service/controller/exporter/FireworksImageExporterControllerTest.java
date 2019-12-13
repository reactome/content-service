package org.reactome.server.service.controller.exporter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class FireworksImageExporterControllerTest extends BaseTest {

    @Test
    public void diagramImage() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("quality", "5");
        params.put("flg", "PTEN");
        params.put("title", true);
        params.put("margin", 10);
        params.put("diagramProfile", "Copper");
        params.put("resource", "total");
        params.put("expColumn", "2");
        params.put("coverage", false);

        mvcGetResult("/exporter/fireworks/9606.svg", "image/svg+xml;", params);

        mvcGetResult("/exporter/fireworks/9606.png", "image/png;", params);
    }
}