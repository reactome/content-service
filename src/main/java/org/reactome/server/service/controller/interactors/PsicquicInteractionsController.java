package org.reactome.server.service.controller.interactors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.exception.PsicquicResourceNotFoundException;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("unused")
@Tag(name = "interactors", description = "Molecule interactors")
@RequestMapping("/interactors/psicquic")
@RestController
public class PsicquicInteractionsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    // Number of accessions that will define if we run in one thread or two
    private static final int THRESHOLD = 10;

    @Autowired
    private InteractionManager interactions;

    @Operation(summary = "Retrieve a list of all Psicquic Registries services")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<PsicquicResource> getResources() {
        // This method is invoked by Spring Scheduler, we are logging this all the time. Keep it as debug
        infoLogger.debug("Querying Psicquic Resources");

        // Get values from the in-memory list
        List<PsicquicResource> resources = PsicquicResourceCachingScheduler.getPsicquicResources();

        // Resources will be null in case the Scheduler couldn't query PSICQUIC
        if (resources == null) {
            throw new PsicquicContentException("Couldn't load PSICQUIC Resources");
        }

        return resources;
    }

    @Operation(summary = "Retrieve clustered interaction, sorted by score, of a given accession by resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecule/{resource}/{acc}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinDetailsByResource(@Parameter(description = "PSICQUIC Resource", required = true, example = "MINT") @PathVariable String resource,
                                                   @Parameter(description = "Single Accession", required = true, example = "Q13501") @PathVariable String acc) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException, PsicquicResourceNotFoundException {
        infoLogger.info("Psicquic details query for resource {} and accession {}", resource, acc);
        return interactions.getPsicquicProteinsDetails(Collections.singletonList(acc), resource);
    }

    @Operation(summary = "Retrieve clustered interaction, sorted by score, of a given accession(s) by resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecules/{resource}/details", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsDetailsByResource(@Parameter(description = "PSICQUIC Resource", required = true) @PathVariable String resource,
                                                    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "<b>Accessions</b>", required = true)
                                                    @RequestBody String proteins) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException, PsicquicResourceNotFoundException {
        infoLogger.info("Psicquic details query for resource {} by POST", resource);
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        return interactions.getPsicquicProteinsDetails(accs, resource, accs.size() < THRESHOLD ? 1 : 2, true);
    }

    @Operation(summary = "Retrieve a summary of a given accession by resource")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecule/{resource}/{acc}/summary", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Interactors getProteinSummaryByResource(@Parameter(description = "PSICQUIC Resource", required = true, example = "MINT") @PathVariable String resource,
                                                   @Parameter(description = "Single Accession", required = true, example = "Q13501") @PathVariable String acc) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException, PsicquicResourceNotFoundException {
        infoLogger.info("Psicquic summary query for resource {} and accession {}", resource, acc);
        return interactions.getPsicquicProteinsSummary(Collections.singletonList(acc), resource);
    }

    @Operation(summary = "Retrieve a summary of a given accession list by resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/molecules/{resource}/summary", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Interactors getProteinsSummaryByResource(@Parameter(description = "PSICQUIC Resource", required = true) @PathVariable String resource,
                                                    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "<b>Accessions</b>", required = true)
                                                    @RequestBody String proteins) throws PsicquicQueryException, PsimiTabException, PsicquicRegistryClientException, PsicquicResourceNotFoundException {
        infoLogger.info("Psicquic summary query for resource {} by POST", resource);
        // Split param and put into a Set to avoid duplicates
        Collection<String> accs = new HashSet<>();
        for (String id : proteins.split(",|;|\\n|\\t")) {
            accs.add(id.trim());
        }
        return interactions.getPsicquicProteinsSummary(accs, resource);
    }
}
