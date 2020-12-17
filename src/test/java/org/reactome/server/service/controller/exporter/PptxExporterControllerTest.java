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
public class PptxExporterControllerTest extends BaseTest {

    @Test
    public void diagramPPTX() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("profile", "Modern");
        params.put("flgInteractors", true);
        //pathway
        mockMvcGetResult("/exporter/diagram/R-HSA-68886.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", params);
        //reaction
        mockMvcGetResult("/exporter/reaction/R-HSA-6789031.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", params);
    }
}