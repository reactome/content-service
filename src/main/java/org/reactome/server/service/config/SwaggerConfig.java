package org.reactome.server.service.config;

import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                //Remove Basic Error Controller In SpringFox SwaggerUI
                .apis(RequestHandlerSelectors.basePackage("org.reactome.server"))
                .paths(PathSelectors.any())
                .build()
                .tags(new Tag("exporter", "Reactome Data: Format Exporter"))
                .tags(new Tag("discover", "Reactome Data: Search engines discovery schema"))
                .tags(new Tag("diseases", "Reactome Data: Disease related queries"))
                .tags(new Tag("events", "Reactome Data: Queries related to events"))
                .tags(new Tag("database", "Reactome Data: Database info queries"))
                .tags(new Tag("mapping", "Reactome Data: Mapping related queries"))
                .tags(new Tag("orthology", "Reactome Data: Orthology related queries"))
                .tags(new Tag("participants", "Reactome Data: Queries related to participants"))
                .tags(new Tag("pathways", "Reactome Data: Pathway related queries"))
                .tags(new Tag("person", "Reactome Data: Person queries"))
                .tags(new Tag("entities", "Reactome Data: PhysicalEntity queries"))
                .tags(new Tag("query", "Reactome Data: Common data retrieval"))
                .tags(new Tag("references", "Reactome xRefs: ReferenceEntity queries"))
                .tags(new Tag("schema", "Reactome Data: Schema class queries"))
                .tags(new Tag("species", "Reactome Data: Species related queries"))
                .tags(new Tag("interactors", "Molecule interactors"))
                .tags(new Tag("search", "Reactome Search"))
                .apiInfo(apiInfo());
    }


    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Content Service",
                "REST API for Reactome content",
                "1.2",
                "/license",
                new Contact("Reactome", "https://reactome.org", "help@reactome.org"),
                "Creative Commons Attribution 4.0 International (CC BY 4.0) License",
                "https://creativecommons.org/licenses/by/4.0/",
                Collections.emptyList()
        );
    }
}