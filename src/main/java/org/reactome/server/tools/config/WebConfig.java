package org.reactome.server.tools.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactome.server.tools.interactors.mapper.SynonymMapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@Configuration
@ComponentScan("org.reactome.server.tools")
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    public WebConfig() {
        super();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        converter.setObjectMapper(objectMapper);
        converters.add(converter);

        super.configureMessageConverters(converters);
    }

}
