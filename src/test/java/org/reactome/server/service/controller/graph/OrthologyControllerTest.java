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
public class OrthologyControllerTest extends BaseTest {

    @Test
    public void getOrthology() throws Exception {
        mvcGetResult("/data/orthology/8956320/species/48898", "application/json;Charset=UTF-8");
    }

    //todo post parameter
    @Test
    public void getOrthologies() throws Exception {

        mvcPostResult("/data/orthologies/ids/species/49633", "R-HSA-6799198,R-HSA-446203, R-HSA-4086398");
    }
}