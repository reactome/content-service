package org.reactome.server.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Component
public class ReactomeGraphCoreConfig {

    @Autowired
    public void init(@Value("${neo4j.host}") String host,
                     @Value("${neo4j.user}") String user,
                     @Value("${neo4j.password}") String password) {
        System.setProperty("neo4j.host", host);
        System.setProperty("neo4j.user", user);
        System.setProperty("neo4j.password", password);
    }
}
