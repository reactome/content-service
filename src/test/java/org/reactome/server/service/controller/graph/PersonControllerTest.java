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
public class PersonControllerTest extends BaseTest {

    @Test
    public void findPersonByName() throws Exception {

        //equals
        mvcGetResult("/data/people/name/bijay jassal/exact", "application/json;Charset=UTF-8");

        mvcGetResult("/data/people/name/Bijay Jassal/exact", "application/json;Charset=UTF-8");
    }

    @Test
    public void queryPersonByName() throws Exception {

        //contains
        mvcGetResult("/data/people/name/Bijay Jassal", "application/json;Charset=UTF-8");

        mvcGetResult("/data/people/name/bijay jassal", "application/json;Charset=UTF-8");

        mvcGetResult("/data/people/name/bijay", "application/json;Charset=UTF-8");

        mvcGetResult("/data/people/name/bijay", "application/json;Charset=UTF-8");

    }

    @Test
    public void findPerson() throws Exception {

        //orcid
        mvcGetResult("/data/person/0000-0002-5039-5405", "application/json;Charset=UTF-8");

        //dbid
        mvcGetResult("/data/person/73447", "application/json;Charset=UTF-8");
    }

    @Test
    public void findPersonAttributeName() throws Exception {

        mvcGetResult("/data/person/0000-0002-5039-5405/displayName", "text/plain;charset=ISO-8859-1");

        mvcGetResult("/data/person/73447/displayName", "text/plain;charset=ISO-8859-1");

        mvcGetResult("/data/person/0000-0002-5039-5405/publications", "application/json;charset=UTF-8");

        mvcGetResult("/data/person/0000-0002-5039-5405/affiliation", "text/plain;charset=ISO-8859-1");
    }

    @Test
    public void getPublicationsOfPerson() throws Exception {

        mvcGetResult("/data/person/73447/publications", "application/json;charset=UTF-8");
    }

    @Test
    public void getAuthoredPathways() throws Exception {

        mvcGetResult("/data/person/73447/authoredPathways", "application/json;charset=UTF-8");
    }
}