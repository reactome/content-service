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
    public void findById() throws Exception {

        mvcGetResult("/data/query/8956320", "application/json;Charset=UTF-8");
    }

    @Test
    public void findByIdAttributeName() throws Exception {

        mvcGetResult("/data/query/8956320/displayName", "text/plain;charset=ISO-8859-1");
    }

    @Test
    public void findByIds() throws Exception {

        mvcPostResult("/data/query/ids", "R-HSA-141409, R-HSA-141431, R-HSA-141422");

        mvcPostResultNotFound("/data/query/ids", "R-HSA-141409R-HSA-141431");
    }

    @Test
    public void findByIdsMap() throws Exception {

        mvcPostResult("/data/query/ids/map", "141409,141431,141422");
    }

    @Test
    public void findEnhancedObjectById() throws Exception {

        mvcGetResult("/data/query/enhanced/R-HSA-163200", "application/json;charset=UTF-8");
    }

    //##################### API Ignored but still available for internal purposes #####################//
    @Test
    public void findMoreObjectById() throws Exception {

        mvcGetResult("/data/query/R-HSA-163200/more", "application/json;charset=UTF-8");
    }

    @Test
    public void findByIdNoRelations() throws Exception {

        mvcGetResult("/data/query/abridged/R-HSA-163200", "application/json;Charset=UTF-8");
    }

    @Test
    public void findByIdNoRelationsAttributeName() throws Exception {

        mvcGetResult("/data/query/abridged/R-HSA-163200/displayName", "text/plain;charset=ISO-8859-1");
    }
}