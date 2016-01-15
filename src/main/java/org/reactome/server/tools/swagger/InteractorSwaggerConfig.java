package org.reactome.server.tools.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Configuration
@EnableSwagger2
public class InteractorSwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Reactome Interactors REST Service",
                "Provide easy access to protein or chemical interactions based on IntAct dataset.",
                "API TOS",
                "Terms of service",
                "Guilherme Viteri [gviteri@ebi.ac.uk]",
                "License of API",
                "API license URL");
        return apiInfo;
    }
}