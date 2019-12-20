package org.reactome.server.service.controller.exporter;

import io.swagger.annotations.Api;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class PptxExporterControllerTest extends BaseTest {

    @Test
    public void diagramPPTX() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("profile", "Modern");
        params.put("flgInteractors", true);

        mvcGetResult("/exporter/diagram/R-HSA-68886.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", params);
    }
}