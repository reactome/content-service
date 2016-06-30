package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.model.PsicquicResource;
import org.reactome.server.service.exception.PsicquicContentException;
import org.reactome.server.service.manager.InteractionManager;
import org.reactome.server.service.model.interactors.Interactors;
import org.reactome.server.service.utils.PsicquicResourceCachingScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import psidev.psi.mi.tab.PsimiTabException;

import java.util.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@Api(tags = "interactors", description = "Molecule interactors")
@RequestMapping("/interactors/psicquic")
public class PsicquicInteractionsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private InteractionManager interactions;

    @ApiOperation(value = "Retrieve a list of all Psicquic Registries services", response = PsicquicResource.class, produces = "application/json")
    @RequestMapping(value = "/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<PsicquicResource> getResources() {
        /** Get values from the in-memory list **/
        List<PsicquicResource> resources = PsicquicResourceCachingScheduler.getPsicquicResources();

        /** Resources will be null in case the Scheduler couldn't query PSICQUIC **/
        if (resources == null) {
            throw new PsicquicContentException("Couldn't load PSICQUIC Resources");
        }

        return resources;
    }

    @ApiOperation(value = "Retrieve clustered interaction, sorted by score, of a given accession by resource.", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/{resource}/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinDetailsByResource(@ApiParam(value = "PSICQUIC Resource", required = true) @PathVariable String resource,
                                                   @ApiParam(value = "Single Accession", required = true) @PathVariable String acc) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException {

        return interactions.getPsicquicProteinsDetails(Collections.singletonList(acc), resource);
    }

    @ApiOperation(value = "Retrieve clustered interaction, sorted by score, of a given accession(s) by resource.", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecules/{resource}/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsDetailsByResource(@ApiParam(value = "PSICQUIC Resource", required = true) @PathVariable String resource,
                                                    @ApiParam(value = "Accessions", required = true) @RequestBody String proteins) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return interactions.getPsicquicProteinsDetails(accs, resource);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession by resource", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecule/{resource}/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinSummaryByResource(@ApiParam(value = "PSICQUIC Resource", required = true) @PathVariable String resource,
                                                   @ApiParam(value = "Single Accession", required = true) @PathVariable String acc) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException {

        return interactions.getPsicquicProteinsSummary(Collections.singletonList(acc), resource);
    }

    @ApiOperation(value = "Retrieve a summary of a given accession list by resource.", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/molecules/{resource}/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsSummaryByResource(@ApiParam(value = "PSICQUIC Resource", required = true) @PathVariable String resource,
                                                    @ApiParam(value = "Accessions", required = true) @RequestBody String proteins) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        return interactions.getPsicquicProteinsSummary(accs, resource);
    }
}
