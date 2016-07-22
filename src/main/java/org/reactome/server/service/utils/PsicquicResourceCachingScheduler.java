package org.reactome.server.service.utils;

import org.reactome.server.interactors.exception.PsicquicInteractionClusterException;
import org.reactome.server.interactors.model.PsicquicResource;
import org.reactome.server.interactors.service.PsicquicService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;


/**
 * Scheduler for querying and caching Psicquic Resources
 * This scheduler runs when Tomcat starts and will keep running
 * based on the cron scheduler settings.
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@EnableScheduling
@Component
public class PsicquicResourceCachingScheduler {

    private static final org.slf4j.Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    private static List<PsicquicResource> psicquicResources = null;

    @Autowired
    private PsicquicService psicquicService;

    @PostConstruct
    @Scheduled(cron = "${psicquic.cache.cron}")
    public void queryPsicquicResources() {
        try {
            psicquicResources = psicquicService.getResources();
            infoLogger.debug("Psicquic Resources have been cached.");
        } catch (PsicquicInteractionClusterException e) {
            /**
             * If we do not catch exception here, then Spring won't be able to instantiate the bean
             * and consequentially failed startup.
             * If in the meantime PSICQUIC is down, clean previous cached list and return a 500 HTTP Status as is.
             */
            psicquicResources = null;
            infoLogger.warn("Couldn't load the PSICQUIC Resources");
        }
    }

    public static List<PsicquicResource> getPsicquicResources(){
        return psicquicResources;
    }
}



