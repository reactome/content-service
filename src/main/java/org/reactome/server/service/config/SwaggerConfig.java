package org.reactome.server.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reflections.Reflections;
import org.springdoc.core.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI createRestApi() {
        Reflections reflections = new Reflections(DatabaseObject.class.getPackage().getName());
        SpringDocUtils config = SpringDocUtils.getConfig();
        for (Class<?> clazz : reflections.getSubTypesOf(DatabaseObject.class)) {
            config.replaceWithClass(clazz, Void.class);
        }

        return new OpenAPI()
                .tags(List.of(
                        new Tag().name("exporter").description("Reactome Data: Format Exporter"),
                        new Tag().name("discover").description("Reactome Data: Search engines discovery schema"),
                        new Tag().name("diseases").description("Reactome Data: Disease related queries"),
                        new Tag().name("events").description("Reactome Data: Queries related to events"),
                        new Tag().name("database").description("Reactome Data: Database info queries"),
                        new Tag().name("mapping").description("Reactome Data: Mapping related queries"),
                        new Tag().name("orthology").description("Reactome Data: Orthology related queries"),
                        new Tag().name("participants").description("Reactome Data: Queries related to participants"),
                        new Tag().name("pathways").description("Reactome Data: Pathway related queries"),
                        new Tag().name("person").description("Reactome Data: Person queries"),
                        new Tag().name("entities").description("Reactome Data: PhysicalEntity queries"),
                        new Tag().name("query").description("Reactome Data: Common data retrieval"),
                        new Tag().name("references").description("Reactome xRefs: ReferenceEntity queries"),
                        new Tag().name("schema").description("Reactome Data: Schema class queries"),
                        new Tag().name("species").description("Reactome Data: Species related queries"),
                        new Tag().name("interactors").description("Molecule interactors"),
                        new Tag().name("search").description("Reactome Search"))
                )
                .info(new Info()
                        .title("Content Service")
                        .description("REST API for Reactome content")
                        .version("1.2")
                        .license(new License()
                                .name("Creative Commons Attribution 4.0 International (CC BY 4.0) License")
                                .url("https://creativecommons.org/licenses/by/4.0/")
                        )
                        .termsOfService("/license")
                        .contact(new Contact()
                                .name("Reactome")
                                .email("help@reactome.org")
                                .url("https://reactome.org")
                        )
                );
    }

}