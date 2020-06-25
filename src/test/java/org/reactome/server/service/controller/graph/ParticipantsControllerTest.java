package org.reactome.server.service.controller.graph;

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
public class ParticipantsControllerTest extends BaseTest {

    @Test
    public void getParticipants() throws Exception {
        mockMvcGetResult("/data/participants/6799198", "application/json;Charset=UTF-8");
    }

    @Test
    public void getParticipatingPhysicalEntities() throws Exception {
        mockMvcGetResult("/data/participants/6799198/participatingPhysicalEntities", "application/json;Charset=UTF-8");
    }

    @Test
    public void getParticipatingReferenceEntities() throws Exception {
        mockMvcGetResult("/data/participants/6799198/referenceEntities", "application/json;Charset=UTF-8");
    }
}