package org.reactome.server.tools.manager;

import org.reactome.server.tools.exception.InteractorResourceNotFound;
import org.reactome.server.tools.exception.PsicquicContentException;
import org.reactome.server.tools.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.tools.interactors.exception.PsicquicInteractionClusterException;
import org.reactome.server.tools.interactors.mapper.EntityMapper;
import org.reactome.server.tools.interactors.mapper.InteractionMapper;
import org.reactome.server.tools.interactors.mapper.InteractorMapper;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.InteractionResource;
import org.reactome.server.tools.interactors.model.PsicquicResource;
import org.reactome.server.tools.interactors.service.InteractionResourceService;
import org.reactome.server.tools.interactors.service.InteractionService;
import org.reactome.server.tools.interactors.service.PsicquicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Controller
public class InteractionManager {
    /**
     * Holds the services that query the DB
     **/
    @Autowired
    private InteractionService interactionService;

    @Autowired
    private InteractionResourceService interactionResourceService;

    @Autowired
    private PsicquicService psicquicService;

    /**
     * These attributes will be used to cache the resource
     **/
    private Map<String, InteractionResource> interactionResourceMap;

    public InteractionManager() {
        this.interactionResourceMap = new HashMap<>();
    }

    /**
     * Caching resources for easy access during json handling
     *
     * @throws SQLException
     */
    private void cacheStaticResources() throws SQLException {
        List<InteractionResource> interactionResourceList = interactionResourceService.getAll();
        for (InteractionResource interactionResource : interactionResourceList) {
            interactionResourceMap.put(interactionResource.getName().toLowerCase(), interactionResource);
        }
    }

    /**
     * Retrieve static interactions details
     *
     * @return InteractionMapper
     */
    public InteractionMapper getStaticProteinDetails(Collection<String> accs, String resource, Integer page, Integer pageSize) {
        try {
            /** caching resources, get values from the Map **/
            cacheStaticResources();

            /** Query database. Generic Layer. Don't need to know the DB to communicate here **/
            Map<String, List<Interaction>> interactionMaps = interactionService.getInteractions(accs, resource, page, pageSize);

            return getDetailInteractionResult(interactionMaps, resource);

        } catch (SQLException | InvalidInteractionResourceException s) {
            s.printStackTrace();
            throw new InteractorResourceNotFound(resource);
        }

    }

    /**
     * Retrieve PSICQUIC interactions
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public InteractionMapper getPsicquicProteinsDetails(Collection<String> accs, String resource){
        try {
            /** Query PSICQUIC service and retrieve Interactions sorted by score and higher than 0.45 **/
            Map<String, List<Interaction>> interactionMap = psicquicService.getInteractions(resource, accs);

            return getDetailInteractionResult(interactionMap, resource);
        }catch (PsicquicInteractionClusterException e) {
            throw new PsicquicContentException(e);
        }
    }

    /**
     * Generic method that queries the database and build the JSON Object
     *
     * @return InteractionMapper
     */
    public InteractionMapper getStaticProteinsSummary(Collection<String> accs, String resource) {
        try {
            /** Query database and get the count **/
            Map<String, Integer> interactionCountMap = interactionService.countInteractionsByAccessions(accs, resource);

            return getSummaryInteractionResult(interactionCountMap, resource);

        } catch (SQLException | InvalidInteractionResourceException s) {
            throw new InteractorResourceNotFound(resource);
        }

    }

    /**
     * Retrieve PSICQUIC interactions summary
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public InteractionMapper getPsicquicProteinsSummary(Collection<String> accs, String resource){

        /** Query PSICQUIC service and retrieve Interactions sorted by score and higher than 0.45 **/
        Map<String, Integer> interactionMap;

        try {
            interactionMap = psicquicService.countInteraction(resource, accs);

        } catch (PsicquicInteractionClusterException e) {
            throw new PsicquicContentException(e);
        }

        return getSummaryInteractionResult(interactionMap, resource);
    }


    /**
     * Call psicquic REST service and retrieve all Resources.
     */
    public List<PsicquicResource> getPsicquicResources() {
        try {
            /**
             * TEMPORARY SOLUTION: Remove service with issues.
             *      Theses services are active but throwing exception. We decided to remove them from the list:
             *      MatrixDB, I2D, Spike
             */
            List<PsicquicResource> registries = psicquicService.getResources();

            Iterator<PsicquicResource> iterator = registries.iterator();
            while (iterator.hasNext()) {
                PsicquicResource registry = iterator.next();

                if (registry.getName().equalsIgnoreCase("MatrixDB") ||
                        registry.getName().equalsIgnoreCase("I2D") ||
                        registry.getName().equalsIgnoreCase("Spike")) {
                    iterator.remove();
                }
            }

            return registries;
        }catch (PsicquicInteractionClusterException e) {
            throw new PsicquicContentException(e);
        }


    }

    /**
     * Set up the InteractionResult object of a given map of interactions and Resource.
     * This method is able to parse for static resource and psicquic.
     *
     * @param interactionMaps key=accession and value=List of interactions
     * @param resource resource that can be static or psicquic resource
     * @return InteractionMapper
     */
    public InteractionMapper getDetailInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource){
        InteractionMapper interactionMapper = new InteractionMapper();

        /** Entities are a JSON Object **/
        List<EntityMapper> entities = new ArrayList<>();

        for (String accKey : interactionMaps.keySet()) {

            List<Interaction> interactions = interactionMaps.get(accKey);

            /** Remove from output if there is no interaction **/
            if(interactions.size() == 0) {
                continue;
            }

            interactionMapper.setResource(resource);
            interactionMapper.setInteractorUrl(""); // TODO sometimes a protein interacts with chebi, consider both urls here + type in the json

            /** Get InteractionResource that has been previously cached **/
            InteractionResource interactionResource = interactionResourceMap.get(resource.toLowerCase());
            if(interactionResource != null) {
                interactionMapper.setInteractionUrl(interactionResource.getUrl());
            }

            EntityMapper entity = new EntityMapper();
            entity.setAcc(accKey.trim());
            entity.setCount(interactions.size());

            List<InteractorMapper> interactorsResultList = new ArrayList<>();
            for (Interaction interaction : interactions) {
                InteractorMapper interactor = new InteractorMapper();
                interactor.setAcc(interaction.getInteractorB().getAcc());
                interactor.setScore(interaction.getIntactScore());
                interactor.setAlias(interaction.getInteractorB().getAlias());

                if(interaction.getInteractionDetailsList().size() > 0) {
                    interactor.setId(interaction.getInteractionDetailsList().get(0).getInteractionAc());
                }

                interactorsResultList.add(interactor);
            }

            entity.setInteractors(interactorsResultList);

            entities.add(entity);

            interactionMapper.setEntities(entities);

        }

        return interactionMapper;

    }

    /**
     * Set up the InteractionResult object of a given map of interactions and Resource.
     * This method is able to parse for static resource and psicquic.
     *
     * @param summaryMap key=accession and value=total of interactions
     * @param resource resource that can be static or psicquic resource
     * @return InteractionMapper
     */
    public InteractionMapper getSummaryInteractionResult(Map<String, Integer> summaryMap, String resource){
        InteractionMapper interactionMapper = new InteractionMapper();

        /** Entities are a JSON Object **/
        List<EntityMapper> entities = new ArrayList<>();

        for (String accKey : summaryMap.keySet()) {
            Integer interactionsCount = summaryMap.get(accKey);

            interactionMapper.setResource(resource);

            EntityMapper entity = new EntityMapper();
            entity.setAcc(accKey.trim());
            entity.setCount(interactionsCount);

            /** Remove from output if there is no interaction **/
            if(interactionsCount > 0) {
                entities.add(entity);
            }

        }

        interactionMapper.setEntities(entities);

        return interactionMapper;

    }
}