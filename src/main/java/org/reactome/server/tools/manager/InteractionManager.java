package org.reactome.server.tools.manager;

import org.apache.commons.lang3.StringUtils;
import org.reactome.server.tools.exception.InteractorResourceNotFound;
import org.reactome.server.tools.exception.PsicquicContentException;
import org.reactome.server.tools.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.tools.interactors.exception.PsicquicInteractionClusterException;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.InteractionDetails;
import org.reactome.server.tools.interactors.model.PsicquicResource;
import org.reactome.server.tools.interactors.service.InteractionService;
import org.reactome.server.tools.interactors.service.PsicquicService;
import org.reactome.server.tools.interactors.util.Toolbox;
import org.reactome.server.tools.model.interactors.Interactor;
import org.reactome.server.tools.model.interactors.InteractorEntity;
import org.reactome.server.tools.model.interactors.Interactors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private PsicquicService psicquicService;

    public InteractionManager() {

    }

    /**
     * Retrieve static interactions details
     *
     * @return InteractionMapper
     */
    public Interactors getStaticProteinDetails(Collection<String> accs, String resource, Integer page, Integer pageSize) {
        try {
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
     *
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public Interactors getPsicquicProteinsDetails(Collection<String> accs, String resource) {
        try {
            /** Query PSICQUIC service and retrieve Interactions sorted by score and higher than 0.45 **/
            Map<String, List<Interaction>> interactionMap = psicquicService.getInteractions(resource, accs);

            return getDetailInteractionResult(interactionMap, resource);
        } catch (PsicquicInteractionClusterException e) {
            throw new PsicquicContentException(e);
        }
    }

    /**
     * Generic method that queries the database and build the JSON Object
     *
     * @return InteractionMapper
     */
    public Interactors getStaticProteinsSummary(Collection<String> accs, String resource) {
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
     *
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public Interactors getPsicquicProteinsSummary(Collection<String> accs, String resource) {

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
            return psicquicService.getResources();
        } catch (PsicquicInteractionClusterException e) {
            throw new PsicquicContentException("Couldn't load PSICQUIC Resources");
        }
    }

    /**
     * Set up the InteractionResult object of a given map of interactions and Resource.
     * This method is able to parse for static resource and psicquic.
     *
     * @param interactionMaps key=accession and value=List of interactions
     * @param resource        resource that can be static or psicquic resource
     * @return InteractionMapper
     */
    public Interactors getDetailInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource) {
        return getInteractionResult(interactionMaps, resource, null);
    }

    public Interactors getCustomInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource, String token) {
        return getInteractionResult(interactionMaps, resource, token);
    }

    private Interactors getInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource, String token) {
        Interactors interactionMapper = new Interactors();

        /** Entities are a JSON Object **/
        List<InteractorEntity> entities = new ArrayList<>();

        int count = 1;
        for (String accKey : interactionMaps.keySet()) {

            List<Interaction> interactions = interactionMaps.get(accKey);

            /** Remove from output if there is no interaction **/
            if (interactions.size() == 0) {
                continue;
            }

            InteractorEntity entity = new InteractorEntity();
            entity.setAcc(accKey.trim());
            entity.setCount(interactions.size());

            List<Interactor> interactorsResultList = new ArrayList<>();
            for (Interaction interaction : interactions) {
                Interactor interactor = new Interactor();
                interactor.setAcc(interaction.getInteractorB().getAcc());
                interactor.setScore(interaction.getIntactScore());

                interactor.setAlias(interaction.getInteractorB().getAliasWithoutSpecies(true));

                /** Set Id as auto increment **/
                interactor.setId(count++);

                /** This list holds evidences that we are going to use to build the evidences URL. **/
                List<String> evidencesWithDbNames = new ArrayList<>();

                /** Set Evidences as the others Interactions identifiers **/
                if(interaction.getInteractionDetailsList() != null) {
                    for (InteractionDetails interactionDetail : interaction.getInteractionDetailsList()) {
                        String evidence = interactionDetail.getInteractionAc();
                        evidencesWithDbNames.add(evidence);
                    }
                }

                if(interaction.getInteractionDetailsList() != null && interaction.getInteractionDetailsList().size() > 0) {
                    interactor.setEvidences(interaction.getInteractionDetailsList().size());
                }

                /** Accession URL **/
                interactor.setAccURL(Toolbox.getAccessionURL(interaction.getInteractorB().getAcc(), resource));

                /** Interaction URL **/
                interactor.setEvidencesURL(Toolbox.getEvidencesURL(evidencesWithDbNames, resource));

                interactorsResultList.add(interactor);
            }

            entity.setInteractors(interactorsResultList);

            entities.add(entity);
        }

        interactionMapper.setResource(resource);

        // THis is needed for the custom interaction
        if(StringUtils.isNotEmpty(token)) {
            interactionMapper.setResource(token);
        }

        interactionMapper.setEntities(entities);

        return interactionMapper;

    }

    /**
     * Set up the InteractionResult object of a given map of interactions and Resource.
     * This method is able to parse for static resource and psicquic.
     *
     * @param summaryMap key=accession and value=total of interactions
     * @param resource   resource that can be static or psicquic resource
     * @return InteractionMapper
     */
    public Interactors getSummaryInteractionResult(Map<String, Integer> summaryMap, String resource) {
        Interactors interactionMapper = new Interactors();

        /** Entities are a JSON Object **/
        List<InteractorEntity> entities = new ArrayList<>();

        for (String accKey : summaryMap.keySet()) {
            Integer interactionsCount = summaryMap.get(accKey);

            InteractorEntity entity = new InteractorEntity();
            entity.setAcc(accKey.trim());
            entity.setCount(interactionsCount);

            /** Remove from output if there is no interaction **/
            if (interactionsCount > 0) {
                entities.add(entity);
            }
        }

        interactionMapper.setResource(resource);
        interactionMapper.setEntities(entities);

        return interactionMapper;

    }
}