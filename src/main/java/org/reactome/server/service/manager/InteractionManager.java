package org.reactome.server.service.manager;

import org.apache.commons.lang3.StringUtils;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.InteractionsService;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.exception.PsicquicResourceNotFoundException;
import org.reactome.server.interactors.model.Interaction;
import org.reactome.server.interactors.model.InteractionDetails;
import org.reactome.server.interactors.service.PsicquicService;
import org.reactome.server.interactors.util.Toolbox;
import org.reactome.server.service.model.interactors.Interactor;
import org.reactome.server.service.model.interactors.InteractorEntity;
import org.reactome.server.service.model.interactors.Interactors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import psidev.psi.mi.tab.PsimiTabException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Guilherme S Viteri (gviteri@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Component
public class InteractionManager {

    /**
     * Holds the services that query the IntAct interactions in the database
     */
    @Autowired
    private InteractionsService interactionsService;

    @Autowired
    private PsicquicService psicquicService;

    public InteractionManager() { }

    /**
     * Retrieve static interactions details
     *
     * @return InteractionMapper
     */
    public Interactors getStaticProteinDetails(Collection<String> accs, String resource, Integer page, Integer pageSize) {
        Interactors rtn = new Interactors();
        rtn.setResource(resource);
        for (String acc : accs) {
            rtn.add(acc, interactionsService.getInteractions(acc, page, pageSize));
        }
        return rtn;
    }

    /**
     * Retrieve PSICQUIC interactions
     *
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public Interactors getPsicquicProteinsDetails(Collection<String> accs, String resource) throws PsicquicQueryException, PsicquicRegistryClientException, PsimiTabException, PsicquicResourceNotFoundException {
        // Query PSICQUIC service and retrieve Interactions sorted by score and higher than 0.45
        Map<String, List<Interaction>> interactionMap = psicquicService.getInteractions(resource, accs);
        return getDetailInteractionResult(interactionMap, resource);
    }

    /**
     * Retrieve PSICQUIC interactions
     *
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public Interactors getPsicquicProteinsDetails(Collection<String> accs, String resource, int numberOfThreads, boolean cache) throws PsicquicQueryException, PsicquicRegistryClientException, PsimiTabException, PsicquicResourceNotFoundException {
        if (numberOfThreads >= 20) {
            // no more than 20 threads.....
            numberOfThreads = 20;
        }
        // Query PSICQUIC service and retrieve Interactions sorted by score and higher than 0.45
        Map<String, List<Interaction>> interactionMap = psicquicService.getInteractions(resource, accs, numberOfThreads, cache);
        return getDetailInteractionResult(interactionMap, resource);
    }

    /**
     * Generic method that queries the database and build the JSON Object
     *
     * @return InteractionMapper
     */
    public Interactors getStaticProteinsSummary(Collection<String> accs, String resource) {
        Map<String, Integer> interactionCountMap = interactionsService.countInteractionsByAccessions(accs);
        return getSummaryInteractionResult(interactionCountMap, resource);
    }

    /**
     * Retrieve PSICQUIC interactions summary
     *
     * @param resource PSICQUIC Resource
     * @return InteractionMapper which will be serialized to JSON by Jackson
     */
    public Interactors getPsicquicProteinsSummary(Collection<String> accs, String resource) throws PsicquicQueryException, PsicquicRegistryClientException, PsimiTabException, PsicquicResourceNotFoundException {
        // Query PSICQUIC service and retrieve Interactions sorted by score and higher than 0.45
        Map<String, Integer> interactionMap = psicquicService.countInteraction(resource, accs);
        return getSummaryInteractionResult(interactionMap, resource);
    }

    /**
     * Set up the InteractionResult object of a given map of interactions and Resource.
     * This method is able to parse for static resource and psicquic.
     *
     * @param interactionMaps key=accession and value=List of interactions
     * @param resource        resource that can be static or psicquic resource
     * @return InteractionMapper
     */
    private Interactors getDetailInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource) {
        return getInteractionResult(interactionMaps, resource, null);
    }

    public Interactors getCustomInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource, String token) {
        return getInteractionResult(interactionMaps, resource, token);
    }

    public Collection<Pathway> getLowerLevelPathways(String acc, String speciesName, Boolean hasDiagram) {
        if (hasDiagram) {
            return interactionsService.getDiagrammedLowerLevelPathways(acc, speciesName);
        } else {
            return interactionsService.getLowerLevelPathways(acc, speciesName);
        }
    }

    private Interactors getInteractionResult(Map<String, List<Interaction>> interactionMaps, String resource, String token) {
        Interactors interactionMapper = new Interactors();

        // Entities are a JSON Object
        List<InteractorEntity> entities = new ArrayList<>();

        long count = 1;
        for (String accKey : interactionMaps.keySet()) {
            List<Interaction> interactions = interactionMaps.get(accKey);

            // Remove from output if there is no interaction
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
                interactor.setType(interaction.getInteractorB().getType());

                // Set Id as auto increment
                interactor.setId(count++);

                // This list holds evidences that we are going to use to build the evidences URL.
                List<String> evidencesWithDbNames = new ArrayList<>();

                // Set Evidences as the others Interactions identifiers
                if (interaction.getInteractionDetailsList() != null) {
                    for (InteractionDetails interactionDetail : interaction.getInteractionDetailsList()) {
                        String evidence = interactionDetail.getInteractionAc();
                        evidencesWithDbNames.add(evidence);
                    }
                }

                if (interaction.getInteractionDetailsList() != null && interaction.getInteractionDetailsList().size() > 0) {
                    interactor.setEvidences(interaction.getInteractionDetailsList().size());
                }

                // Accession URL
                interactor.setAccURL(Toolbox.getAccessionURL(interaction.getInteractorB().getAcc(), resource));

                // Interaction URL
                interactor.setEvidencesURL(Toolbox.getEvidencesURL(evidencesWithDbNames, resource));
                interactorsResultList.add(interactor);
            }

            entity.setInteractors(interactorsResultList);
            entities.add(entity);
        }

        interactionMapper.setResource(resource);

        // This is needed for the custom interaction
        if (StringUtils.isNotEmpty(token)) {
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
    private Interactors getSummaryInteractionResult(Map<String, Integer> summaryMap, String resource) {
        Interactors interactionMapper = new Interactors();
        // Entities are a JSON Object
        List<InteractorEntity> entities = new ArrayList<>();
        for (String accKey : summaryMap.keySet()) {
            Integer interactionsCount = summaryMap.get(accKey);
            InteractorEntity entity = new InteractorEntity();
            entity.setAcc(accKey.trim());
            entity.setCount(interactionsCount);

            // Remove from output if there is no interaction
            if (interactionsCount > 0) {
                entities.add(entity);
            }
        }
        interactionMapper.setResource(resource);
        interactionMapper.setEntities(entities);
        return interactionMapper;
    }
}