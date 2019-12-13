package org.reactome.server.service.utils;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Component;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class BaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    /**
     * initialize the mockMvc object
     */
    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    /* verify that we're loading the WebApplicationContext object (wac) properly */
    protected void findBeanByName(String beanName) {
        ServletContext servletContext = wac.getServletContext();

        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        Assert.assertNotNull(wac.getBean(beanName));
    }


    /**
     * Testing spring mvc controller get method
     */
    public MvcResult mvcGetResult(String url, String contentType, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mvcGetResult(url, contentType, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mvcGetResult(url, contentType, params);
    }

    public MvcResult mvcGetResult(String url, String contentType) throws Exception {
        return mvcGetResult(url, contentType, null);
    }

    public MvcResult mvcGetResult(String url, String contentType, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);

            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());

            //    params.forEach(requestBuilder::param);
            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType))
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType))
                    .andReturn();
        }
    }

    /**
     * Testing spring mvc controller get 404
     */
    public MvcResult mvcGetResultNotFound(String url, String paramName, String paramValue) throws Exception {

        if (paramName == null && paramValue == null) return mvcGetResultNotFound(url, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mvcGetResultNotFound(url, params);
    }

    public MvcResult mvcGetResultNotFound(String url) throws Exception {
        return mvcGetResultNotFound(url, null);
    }

    public MvcResult mvcGetResultNotFound(String url, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);
            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
            // params.forEach(requestBuilder::param);
            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isNotFound())
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(url))
                    .andExpect(status().isNotFound())
                    .andReturn();
        }
    }


    /**
     * Bad request
     */
    public MvcResult mvcGetResultBadRequest(String url, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);
            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
            // params.forEach(requestBuilder::param);
            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().is5xxServerError())
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(url))
                    .andExpect(status().is5xxServerError())
                    .andReturn();
        }
    }


    /**
     * Testing spring mvc controller post
     */


    public MvcResult mvcPostResult(String url, String content) throws Exception {
        return mvcPostResult(url, content, null);
    }

    public MvcResult mvcPostResult(String url, String content, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mvcPostResult(url, content, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mvcPostResult(url, content, params);
    }


    public MvcResult mvcPostResult(String url, String content, Map<String, Object> params) throws Exception {

        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = post(url)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(content);

            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());

            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))

                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    post(url)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(content))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andReturn();
        }
    }


    /**
     * Testing spring mvc controller post 404
     */
    public MvcResult mvcPostResultNotFound(String url, String content) throws Exception {
        return this.mockMvc.perform(
                post(url)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andReturn();
    }
}
