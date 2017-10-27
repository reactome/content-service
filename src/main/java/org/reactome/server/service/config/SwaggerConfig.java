package org.reactome.server.service.config;

import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        Docket rtn = new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());

        Reflections reflections = new Reflections(DatabaseObject.class.getPackage().getName());
        for (Class<?> clazz : reflections.getSubTypesOf(DatabaseObject.class)) {
            rtn.directModelSubstitute(clazz, Void.class);
        }

        return rtn;
    }

    private ApiInfo apiInfo() {

        return new ApiInfo(
                "Content Service",
                "RESTFul service for Reactome content",
                "1.0",
                "about/license-agreement",
                new Contact("Reactome","http://www.reactome.org","help@reactome.org"),
                "Creative Commons Attribution 3.0 Unported License",
                "http://creativecommons.org/licenses/by/3.0/legalcode",
                Collections.emptyList());
    }

}