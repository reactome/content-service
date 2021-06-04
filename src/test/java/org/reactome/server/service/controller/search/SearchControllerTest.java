package org.reactome.server.service.controller.search;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SearchControllerTest extends BaseTest {

    @Test
    public void spellcheckerSuggestions() throws Exception {
        mockMvcGetResult("/search/spellcheck/", "application/json;Charset=UTF-8", "query", "matablism");
    }

    @Test
    public void suggesterSuggestions() throws Exception {
        mockMvcGetResult("/search/suggest/", "application/json;Charset=UTF-8", "query", "cell");
    }

    @Test
    public void facet() throws Exception {
        mockMvcGetResult("/search/facet", "application/json;charset=UTF-8");
    }

    @Test
    public void facet_type() throws Exception {
        this.getMockMvc().perform(get("/search/facet_query")
                .param("query", "PTEN")
                .param("species", "Homo sapiens")
                .param("species", "Rattus norvegicus")
                .param("types", "Pathway")
                .param("compartments", "cotosol"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
    }

    @Test
    public void getResult() throws Exception {
        this.getMockMvc().perform(get("/search/query")
                .param("query", "enzyme")
                .param("species", "Homo sapiens")
                .param("species", "Rattus norvegicus")
                .param("types", "Pathway")
                .param("parserType", "STD")
                .param("cluster", "true"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getResultPaginated() throws Exception {
        this.getMockMvc().perform(get("/search/query/paginated")
                .param("query", "enzyme")
                .param("species", "Homo sapiens")
                .param("species", "Rattus norvegicus")
                .param("page", "1")
                .param("rowCount", "20")
                .param("types", "Pathway")
                .param("parserType", "STD")
                .param("cluster", "true"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getFireworksResult() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("query", "RAF");
        params.put("species", "Homo sapiens");

        mockMvcGetResult("/search/fireworks", "application/json;charset=UTF-8", params);
    }

    @Test
    public void fireworksFlagging() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("query", "PTEN");
        params.put("species", "Homo sapiens");

        mockMvcGetResult("/search/fireworks/flag", "application/json;charset=UTF-8", params);
    }

    @Test
    public void getDiagramResult() throws Exception {
        mockMvcGetResult("/search/diagram/R-HSA-69620", "application/json;charset=UTF-8", "query", "PTEN");
    }

    @Test
    public void getDiagramOccurrences() throws Exception {
        mockMvcGetResult("/search/diagram/R-HSA-68886/occurrences/R-HSA-141433", "application/json;Charset=UTF-8");
    }

    @Test
    public void getEntitiesInDiagramForIdentifier() throws Exception {
        mockMvcGetResult("/search/diagram/R-HSA-1632852/flag", "application/json;Charset=UTF-8", "query", "ATG13");
    }

    //##################### API Ignored  #####################//
    @Test
    public void diagramSearchSummary() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("query", "KIF");
        params.put("species", "Homo sapiens");
        params.put("diagram", "R-HSA-8848021");

        mockMvcGetResult("/search/diagram/summary", "application/json;Charset=UTF-8", params);
    }
}