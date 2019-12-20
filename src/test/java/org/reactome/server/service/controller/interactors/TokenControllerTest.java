package org.reactome.server.service.controller.interactors;

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
public class TokenControllerTest extends BaseTest {

    @Test
    public void getInteractors() throws Exception {

        //Todo this token only works for local testing
        mvcPostResult("/interactors/token/PSI--1403260106", "Q9UBU9");
    }
}