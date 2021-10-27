package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class PersonControllerTest extends BaseTest {

    @Test
    public void findPersonByName() throws Exception {
        //equals
        mockMvcGetResult("/data/people/name/bijay jassal/exact", "application/json;charset=UTF-8");
        mockMvcGetResult("/data/people/name/Bijay Jassal/exact", "application/json;charset=UTF-8");
    }

    @Test
    public void queryPersonByName() throws Exception {
        //contains
        mockMvcGetResult("/data/people/name/Bijay Jassal", "application/json;charset=UTF-8");
        mockMvcGetResult("/data/people/name/bijay jassal", "application/json;charset=UTF-8");
        mockMvcGetResult("/data/people/name/bijay", "application/json;charset=UTF-8");
        mockMvcGetResult("/data/people/name/bijay", "application/json;charset=UTF-8");

    }

    @Test
    public void findPerson() throws Exception {
        //orcid
        mockMvcGetResult("/data/person/0000-0001-5041-1316", "application/json;charset=UTF-8");
        //dbid
        mockMvcGetResult("/data/person/73447", "application/json;charset=UTF-8");
    }

    @Test
    public void findPersonAttributeName() throws Exception {
        //external user's orcid
        mockMvcGetResult("/data/person/0000-0001-5041-1316/displayName", "text/plain;charset=UTF-8");
        mockMvcGetResult("/data/person/73447/displayName", "text/plain;charset=UTF-8");
        mockMvcGetResult("/data/person/0000-0002-5039-5405/publications", "application/json;charset=UTF-8");
    }

    @Test
    public void getPublicationsOfPerson() throws Exception {
        mockMvcGetResult("/data/person/73447/publications", "application/json;charset=UTF-8");
    }

    @Test
    public void getAuthoredPathways() throws Exception {
        mockMvcGetResult("/data/person/73447/authoredPathways", "application/json;charset=UTF-8");
    }
}