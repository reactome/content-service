package org.reactome.server.tools.manager;

import org.reactome.server.tools.exception.ContentServiceException;
import org.reactome.server.tools.exception.InteractorResourceNotFound;
import org.reactome.server.tools.exception.PsicquicContentException;
import org.reactome.server.tools.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.tools.interactors.exception.PsicquicInteractionClusterException;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.InteractionResource;
import org.reactome.server.tools.interactors.model.PsicquicResource;
import org.reactome.server.tools.interactors.service.InteractionResourceService;
import org.reactome.server.tools.interactors.service.InteractionService;
import org.reactome.server.tools.interactors.service.PsicquicService;
import org.reactome.server.tools.model.interactions.Entity;
import org.reactome.server.tools.model.interactions.InteractionResult;
import org.reactome.server.tools.model.interactions.InteractorResult;
import org.reactome.server.tools.model.interactions.Synonym;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
     * @return InteractionResult
     */
    public InteractionResult getStaticProteinDetails(Collection<String> accs, String resource, Integer page, Integer pageSize) {
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
     * @return InteractionResult which will be serialized to JSON by Jackson
     */
    public InteractionResult getPsicquicProteinsDetails(Collection<String> accs, String resource){
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
     * @return InteractionResult
     */
    public InteractionResult getStaticProteinsSummary(Collection<String> accs, String resource) {
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
     * @return InteractionResult which will be serialized to JSON by Jackson
     */
    public InteractionResult getPsicquicProteinsSummary(Collection<String> accs, String resource){

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
     * @return InteractionResult
     */
    public InteractionResult getDetailInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource){
        InteractionResult interactionResult = new InteractionResult();

        /** Entities are a JSON Object **/
        List<Entity> entities = new ArrayList<>();

        /** Synomys are a JSON Object **/
        Map<String, Synonym> synonymsMaps = new HashMap<>();

        for (String accKey : interactionMaps.keySet()) {

            List<Interaction> interactions = interactionMaps.get(accKey);

            interactionResult.setResource(resource);
            interactionResult.setInteractorUrl(""); // TODO sometimes a protein interacts with chebi, consider both urls here + type in the json

            /** Get InteractionResource that has been previously cached **/
            InteractionResource interactionResource = interactionResourceMap.get(resource.toLowerCase());
            if(interactionResource != null) {
                interactionResult.setInteractionUrl(interactionResource.getUrl());
            }

            Entity entity = new Entity();
            entity.setAcc(accKey.trim());
            entity.setCount(interactions.size());

            List<InteractorResult> interactorsResultList = new ArrayList<>();
            for (Interaction interaction : interactions) {
                InteractorResult interactor = new InteractorResult();
                interactor.setAcc(interaction.getInteractorB().getAcc());
                interactor.setScore(interaction.getIntactScore());

                if(interaction.getInteractionDetailsList().size() > 0) {
                    interactor.setInteractionId(interaction.getInteractionDetailsList().get(0).getInteractionAc());
                }

                /** Creating synonym **/
                Synonym synonym = new Synonym();
                synonym.setAcc(interaction.getInteractorB().getAcc());
                synonym.setImageUrl(null); // TODO define image in the interactor ?
                synonym.setText(interaction.getInteractorB().getAlias());
                synonymsMaps.put(synonym.getAcc(), synonym);

                interactorsResultList.add(interactor);
            }

            entity.setInteractors(interactorsResultList);

            entities.add(entity);

            interactionResult.setEntities(entities);

            interactionResult.setSynonym(synonymsMaps);

        }

        return interactionResult;

    }

    /**
     * Set up the InteractionResult object of a given map of interactions and Resource.
     * This method is able to parse for static resource and psicquic.
     *
     * @param summaryMap key=accession and value=total of interactions
     * @param resource resource that can be static or psicquic resource
     * @return InteractionResult
     */
    public InteractionResult getSummaryInteractionResult(Map<String, Integer> summaryMap, String resource){
        InteractionResult interactionResult = new InteractionResult();

        /** Entities are a JSON Object **/
        List<Entity> entities = new ArrayList<>();

        for (String accKey : summaryMap.keySet()) {
            Integer interactions = summaryMap.get(accKey);

            interactionResult.setResource(resource);

            Entity entity = new Entity();
            entity.setAcc(accKey.trim());
            entity.setCount(interactions);

            entities.add(entity);

        }

        interactionResult.setEntities(entities);

        return interactionResult;

    }
}