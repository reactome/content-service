package org.reactome.server.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Using MockMvc with Entire Application Context
 *
 * @SpringBootTest This annotation uses the SpringBootTestContextBootstrapper class to create the application context.
 * When you use @SpringBootTest, all beans configured in your application are added to the context.
 * @AutoConfigureMockMvc annotation will automatically configure the MockMvc object when used in combination with @SpringBootTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class BaseTest {

    @Autowired
    private MockMvc mockMvc;

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    /**
     * Get request testing of Spring MVC controllers
     */
    public MvcResult mockMvcGetResult(String url, String contentType, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mockMvcGetResult(url, contentType, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mockMvcGetResult(url, contentType, params);
    }

    public MvcResult mockMvcGetResult(String url, String contentType) throws Exception {
        return mockMvcGetResult(url, contentType, null);
    }

    public MvcResult mockMvcGetResult(String url) throws Exception {
        return mockMvcGetResult(url, null, null);
    }

    public MvcResult mockMvcGetResult(String url, String contentType, Map<String, Object> params) throws Exception {
        if (contentType == null) {
            return this.mockMvc.perform(
                            get(url))
                    .andExpect(status().isOk())
                    .andReturn();
        }

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
     * Get request not found testing of Spring MVC controllers
     */
    public MvcResult mockMvcGetResultNotFound(String url, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mockMvcGetResultNotFound(url, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mockMvcGetResultNotFound(url, params);
    }

    public MvcResult mockMvcGetResultNotFound(String url) throws Exception {
        return mockMvcGetResultNotFound(url, null);
    }

    public MvcResult mockMvcGetResultNotFound(String url, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);
            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
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
     * Bad request testing of Spring MVC controllers
     */
    public MvcResult mockMvcGetResultBadRequest(String url, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);
            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
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
     * Post request testing of Spring MVC controllers
     */
    public MvcResult mockMvcPostResult(String url, String content) throws Exception {
        return mockMvcPostResult(url, content, null);
    }

    public MvcResult mockMvcPostResult(String url, String content, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mockMvcPostResult(url, content, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mockMvcPostResult(url, content, params);
    }

    public MvcResult mockMvcPostResult(String url, String content, Map<String, Object> params) throws Exception {
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
     * post not found request testing of Spring MVC controllers
     */
    public MvcResult mvcPostResultNotFound(String url, String content) throws Exception {
        return this.mockMvc.perform(
                        post(url)
                                .contentType(MediaType.TEXT_PLAIN)
                                .content(content))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
    }
}
