package org.reactome.server.service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reflections.Reflections;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Configuration
public class SwaggerConfig {

    @Lazy
    @Bean
    public OpenAPI createRestApi(ServletContext context) {
        Reflections reflections = new Reflections(DatabaseObject.class.getPackage().getName());
        SpringDocUtils config = SpringDocUtils.getConfig();
        for (Class<?> clazz : reflections.getSubTypesOf(DatabaseObject.class)) {
            config.replaceWithClass(clazz, Void.class);
        }


        return new OpenAPI()
                .addServersItem(new Server().url(context.getContextPath()))
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

    @Bean

    public OpenApiCustomiser addGlobalQueryParameters(
            Parameter viewParameter,
            Parameter includeRefParameter
    ) {
        return openApi -> openApi.getPaths().values().forEach(path ->
                path.readOperations().forEach(operation ->
                        operation
                                .addParametersItem(viewParameter)
                                .addParametersItem(includeRefParameter)
                )
        );
    }

    @Bean
    public Parameter viewParameter() {
        return new Parameter()
                .in("query")
                .name("view")
                .description("Global parameter - Customise the view to be more concise and/or aggregate relationships")
                .required(false)
                .schema(
                        new StringSchema()
                                ._enum(List.of("flatten", "nested", "nested-aggregated"))
                                ._default("flatten")
                                .example(null)
                );
    }

    @Bean
    public Parameter includeRefParameter() {
        return new Parameter()
                .in("query")
                .name("includeRef")
                .description("Global parameter - If true, replace element ref to dbId by standard JSOG object {@ref}")
                .required(false)
                .schema(new BooleanSchema()._default(false).example(null));
    }

}