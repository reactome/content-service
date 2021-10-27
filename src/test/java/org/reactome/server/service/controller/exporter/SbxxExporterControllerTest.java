package org.reactome.server.service.controller.exporter;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class SbxxExporterControllerTest extends BaseTest {

    @Test
    public void eventSBGN() throws Exception {
        //pathway
        //todo ERROR: Trying to write a SBGN file that already exists, but test passed
        mockMvcGetResult("/exporter/event/68886.sbgn", "application/sbgn+xml;charset=UTF-8");
        //reaction
        mockMvcGetResult("/exporter/event/R-HSA-5205682.sbgn", "application/sbgn+xml;charset=UTF-8");
    }

    @Test
    public void eventSBML() throws Exception {
        //pathway
        mockMvcGetResult("/exporter/event/R-HSA-157118.sbml", "application/sbml+xml;charset=UTF-8");
        //reaction
        mockMvcGetResult("/exporter/event/R-HSA-5205682.sbml", "application/sbml+xml;charset=UTF-8");
        //pathway in V73
        mockMvcGetResult("/exporter/event/R-HSA-9679191.sbml", "application/sbml+xml;charset=UTF-8");
    }
}