package org.reactome.server.service.controller.exporter;

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
public class SbxxExporterControllerTest extends BaseTest {

    @Test
    public void eventSBGN() throws Exception {

        //pathway
        //todo ERROR: Trying to write a SBGN file that already exists, but test passed
        mvcGetResult("/exporter/event/68886.sbgnnn", "application/sbgn+xml");

        //reaction
        mvcGetResult("/exporter/event/R-HSA-5205682.sbgn","application/sbgn+xml");
    }

    @Test
    public void eventSBML() throws Exception {

        //pathway
        mvcGetResult("/exporter/event/R-HSA-157118.sbml", "application/sbml+xml");

        //reaction
        mvcGetResult("/exporter/event/R-HSA-5205682.sbml", "application/sbml+xml");
    }
}