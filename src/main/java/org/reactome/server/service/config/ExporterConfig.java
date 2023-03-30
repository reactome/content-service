package org.reactome.server.service.config;

import org.reactome.server.tools.diagram.exporter.common.profiles.service.DiagramExporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
public class ExporterConfig {
    Logger log = LoggerFactory.getLogger("threadLogger");
    @Autowired
    public ExporterConfig(ResourceLoader loader) throws IOException {
        Resource resource = loader.getResource("resources/fonts");
        String fontPath = resource.getFile().getAbsolutePath();
        log.debug("Configuring Diagram Exporter with font path : " + fontPath);
        DiagramExporterService.configureFontPath(fontPath);
    }
}
