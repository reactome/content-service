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
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class EventPdfControllerTest extends BaseTest {

    @Test
    public void eventPdf() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("level", 1);
        params.put("diagramProfile", "Modern");
        params.put("resource", "total");
        params.put("expColumn", 1);
        params.put("analysisProfile", "Standard");

        mockMvcGetResult("/exporter/document/event/R-HSA-1632852.pdf", "application/pdf", params);
    }
}