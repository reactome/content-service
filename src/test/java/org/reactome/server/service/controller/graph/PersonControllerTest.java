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
    public void findPersonByName() {
    }

    @Test
    public void queryPersonByName() {
    }

    @Test
    public void findPerson() {
    }

    @Test
    public void testFindPerson() {
    }

    @Test
    public void getPublicationsOfPerson() {
    }

    @Test
    public void getAuthoredPathways() {
    }
}