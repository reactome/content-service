package org.reactome.server.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = {"org.reactome.server"})
//@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
//        pattern = {"org.reactome.server.service.controller.graph.*", "org.reactome.server.service.controller.search.*"}))
@EntityScan({"org.reactome.server.graph.domain.model","org.reactome.server.graph.domain.model"})
@EnableNeo4jRepositories("org.reactome.server.graph.repository")
//todo: check below
@EnableAsync
@EnableScheduling
public class ContentServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ContentServiceApplication.class);
    }


    public static void main(String[] args) {

        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
