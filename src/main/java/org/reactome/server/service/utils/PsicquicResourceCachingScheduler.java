package org.reactome.server.service.utils;

import org.reactome.server.interactors.exception.PsicquicInteractionClusterException;
import org.reactome.server.interactors.model.PsicquicResource;
import org.reactome.server.interactors.service.PsicquicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;


/**
 * Scheduler for querying and caching Psicquic Resources
 * This scheduler runs when Tomcat starts and will keep running
 * based on the cron scheduler settings.
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Component
public class PsicquicResourceCachingScheduler extends Thread {

    private static Logger logger = LoggerFactory.getLogger("threadLogger");

    private static List<PsicquicResource> psicquicResources = null;

    private final PsicquicService psicquicService;
    private boolean active = true;

    @Autowired
    public PsicquicResourceCachingScheduler(PsicquicService psicquicService) {
        super("PsicquicResource");
        this.psicquicService = psicquicService;
        start();
    }

    @Override
    public void run() {
        try {
            while (active) {
                try {
                    psicquicResources = psicquicService.getResources();
                    logger.debug("Psicquic Resources have been updated.");
                } catch (PsicquicInteractionClusterException e) {
                    // If we do not catch exception here, then Spring won't be able to instantiate the bean
                    // and consequentially failed startup.
                    // If in the meantime PSICQUIC is down, clean previous cached list and return a 500 HTTP Status as is.
                    psicquicResources = null;
                    logger.warn("Could not update the PSICQUIC Resources");
                }
                if (active) Thread.sleep(1000 * 60 * 5);
            }
        } catch (InterruptedException e) {
            logger.info("Content-Service PsicquicResourceCachingScheduler interrupted");
        }
    }

    public static List<PsicquicResource> getPsicquicResources(){
        return psicquicResources;
    }

    @PreDestroy
    @Override
    public void interrupt() {
        active = false;
        super.interrupt();
        logger.info("Content-Service PsicquicResourceCachingScheduler stopped");
    }
}



