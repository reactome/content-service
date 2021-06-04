package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.http.MediaType;


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