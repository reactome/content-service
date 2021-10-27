package org.reactome.server.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        Docket rtn = new Docket(DocumentationType.SWAGGER_2)
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

        /*
           TODO: need a better solution
           Swagger UI freezing when expanding APIs with deeply nested data models,we use the directModelSubstitute to fix it in the
           past. Reactome updates all softwares in 2021, in this case, the swagger UI has been updated to 3.0.0 and the code doesn't
           work perfectly as before, it produces the Resolver error in the console and the swagger page.

            --Resolver error at paths./data/pathways/top/{species}.get.responses.200.schema.items.$ref
            --Could not resolve reference: Could not resolve pointer: /definitions/Error-ModelName{namespace='org.reactome.server.graph.domain.model', name='Pathway'} does not exist in document

            FIXING: 1. add springfox.documentation.swagger.use-model-v3=false to service.properties
                        a. this config also produce resolver error but won't print any error message in the console and the endpoints works like normal
                    2. hide the error dialog in the swagger/custom.css

           REFERENCE:  1.https://github.com/springfox/springfox/issues/3476
                       2.https://github.com/swagger-api/swagger-ui/issues/6197
                       3.
         */

//        Reflections reflections = new Reflections(DatabaseObject.class.getPackage().getName());
//        for (Class<?> clazz : reflections.getSubTypesOf(DatabaseObject.class)) {
//            rtn.directModelSubstitute(clazz, Void.class);
//        }
        return rtn;
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