package org.reactome.server.service.controller.graph;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources-test/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class QueryObjectControllerTest extends BaseTest {

    @Test
    public void findById() {
    }

    @Test
    public void testFindById() {
    }

    @Test
    public void findByIds() throws Exception {

        mvcPostResult("/data/query/ids", "R-HSA-141409, R-HSA-141431, R-HSA-141422");

        mvcPostResultNotFound("/data/query/ids", "R-HSA-141409R-HSA-141431");
    }

    @Test
    public void findByIdsMap() {
    }

    @Test
    public void findEnhancedObjectById() {
    }

    @Test
    public void findMoreObjectById() {
    }

    @Test
    public void findByIdNoRelations() {
    }

    @Test
    public void testFindByIdNoRelations() {
    }
}