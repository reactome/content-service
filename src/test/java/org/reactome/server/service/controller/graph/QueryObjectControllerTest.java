package org.reactome.server.service.controller.graph;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;


public class QueryObjectControllerTest extends BaseTest {

    @Test
    public void findById() throws Exception {
        mockMvcGetResult("/data/query/R-HSA-69620", "application/json;Charset=UTF-8");
    }

    @Test
    public void findByIdAttributeName() throws Exception {
        mockMvcGetResult("/data/query/8956320/displayName", "text/plain;charset=UTF-8");
    }

    @Test
    public void findByIds() throws Exception {
        mockMvcPostResult("/data/query/ids", "R-HSA-141409, R-HSA-141431, R-HSA-141422");
        mvcPostResultNotFound("/data/query/ids", "R-HSA-141409R-HSA-141431");
    }

    @Test
    public void findByIdsMap() throws Exception {
        mockMvcPostResult("/data/query/ids/map", "141409,141431,141422");
    }

    @Test
    public void findEnhancedObjectById() throws Exception {
        mockMvcGetResult("/data/query/enhanced/R-HSA-9612973", "application/json;charset=UTF-8");
    }

    //##################### API Ignored but still available for internal purposes #####################//
    @Test
    public void findMoreObjectById() throws Exception {
        mockMvcGetResult("/data/query/R-HSA-9612973/more", "application/json;charset=UTF-8");
    }

    @Test
    public void findByIdNoRelations() throws Exception {
        mockMvcGetResult("/data/query/abridged/R-HSA-9612973", "application/json;Charset=UTF-8");
    }

    @Test
    public void findByIdNoRelationsAttributeName() throws Exception {
        mockMvcGetResult("/data/query/abridged/R-HSA-9612973/displayName", "text/plain;charset=UTF-8");
    }
}