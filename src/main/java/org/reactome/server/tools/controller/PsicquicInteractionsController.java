package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.PsicquicResource;
import org.reactome.server.tools.interactors.service.PsicquicService;
import org.reactome.server.tools.manager.InteractionManager;
import org.reactome.server.tools.model.interactions.Entity;
import org.reactome.server.tools.model.interactions.InteractionResult;
import org.reactome.server.tools.model.interactions.InteractorResult;
import org.reactome.server.tools.model.interactions.Synonym;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@Api(value = "/interactors", description = "Molecule interactors")
@RequestMapping("/interactors/psicquic")
public class PsicquicInteractionsController {

    @Autowired
    private PsicquicService psicquicService;

    @Autowired
    private InteractionManager interactions;

    @ApiOperation(value = "Retrieve a list of all Psicquic Registries services", response = PsicquicResource.class)
    @RequestMapping(value = "/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<PsicquicResource> getResources()  {

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
    }

    @ApiOperation(value = "Retrieve an interactors list of a given accession and registry.", response = InteractionResult.class)
    @RequestMapping(value = "/{resource}/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InteractionResult getInteractorFromPsicquic(@PathVariable String resource,
                                                       @PathVariable String acc)  {
        List<Interaction> interactions = psicquicService.getInteractions(resource, acc);

        InteractionResult interactionResult = new InteractionResult();

        /** Entities are a JSON Object **/
        List<Entity> entities = new ArrayList<>();

        /** Synomys are a JSON Object **/
        Map<String, Synonym> synonymsMaps = new HashMap<>();

//        for (String accKey : interactionMaps.keySet()) {
//
//            List<Interaction> interactions = interactionMaps.get(accKey);

            interactionResult.setResource(resource);
            interactionResult.setInteractorUrl(""); // TODO sometimes a protein interacts with chebi, consider both urls here + type in the json
            interactionResult.setInteractionUrl("");

            Entity entity = new Entity();
            entity.setAcc(acc);
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

//        }

        return interactionResult;

    }

    @ApiOperation(value = "Retrieve a summary of a given accession list by resource", response = InteractionResult.class)
    @RequestMapping(value = "/proteins/{resource}/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public InteractionResult getProteinsSummaryByResource(@PathVariable String resource,
                                                          @RequestBody String proteins) {
        //List<Interaction> interactions = psicquicService.getInteractions(resource, acc);

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return new InteractionResult();

    }

    @ApiOperation(value = "Retrieve a detailed interaction information of a given accession by resource", response = InteractionResult.class)
    @RequestMapping(value = "/proteins/{resource}/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public InteractionResult getProteinsDetailsByResource(@RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
                                                          @RequestParam(value = "pageSize", required = false, defaultValue = "-1") Integer pageSize,
                                                          @PathVariable String resource,
                                                          @RequestBody String proteins) {
        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));
        Map<String, List<Interaction>> interactionMap = psicquicService.getInteractions(resource, accs);

        return interactions.test(interactionMap, resource);
    }

}
