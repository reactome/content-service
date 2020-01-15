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
public class ImageExporterControllerTest extends BaseTest {

    @Test
    public void getBean() {
        findBeanByName("imageExporterController");
    }

    @Test
    public void diagramImage() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("quality", 5);
        //solr
        params.put("flg", "UNC5B");
        params.put("flgInteractors", true);
        params.put("title", false);
        params.put("diagramProfile", "Modern");
        params.put("resource", "total");

        //pathway
        mvcGetResult("/exporter/diagram/166520.png", "image/png", params);

        //reaction
        mvcGetResult("/exporter/diagram/6789031.png", "image/png", params);
    }

    @Test
    public void reactionImage() throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("quality", 5);
        params.put("flgInteractors", true);
        params.put("resource", "total");
        mvcGetResult("/exporter/reaction/70272.jpg", "image/jpg", params);
    }
}