package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.tuple.model.TupleResult;
import org.reactome.server.tools.manager.CustomInteractorManager;
import org.reactome.server.tools.manager.InteractionManager;
import org.reactome.server.tools.model.interactors.Interactors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */


@Api(tags = "interactors", description = "Molecule interactors")
@RequestMapping(value = "/interactors/upload/tuple")
@RestController
public class CustomInteractorsController {

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;


    @ApiOperation(value = "Parse file and retrieve a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public TupleResult postFile(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                                @RequestParam(required = true) String name,
                                @ApiParam(name = "file", required = true, value = "Upload your custom interactor file")
                                @RequestPart(required = true) MultipartFile file) throws IOException {

        return customInteractionManager.getUserDataContainer(name, file.getName(), file.getInputStream());
    }

    @ApiOperation(value = "Paste file content and get a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postFileContent(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                                       @RequestParam(required = true) String name,
                                       @ApiParam(name = "file content", value = "Paste custom interactors file content", required = true)
                                       @RequestBody String fileContent) {

        return customInteractionManager.getUserDataContainer(name, null, fileContent);
    }

    @ApiOperation(value = "Send file via URL and get a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postUrl(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                               @RequestParam(required = true) String name,
                               @ApiParam(name = "url", required = true, value = "A URL pointing to the Interactors file")
                               @RequestBody String url) {

        String fileNamefromUrl = customInteractionManager.getFileNameFromURL(url);

        return customInteractionManager.getUserDataContainerFromURL(name, fileNamefromUrl, url);

    }

    @ApiOperation(value = "Retrieve a summary of a given accession", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/token/{token}", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public Interactors getInteractors(@ApiParam(name = "token", required = true, value = "A token associated with you data submission")
                                      @PathVariable String token,
                                      @ApiParam(value = "Interactor accessions", required = true)
                                      @RequestBody String proteins) {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        Map<String, List<Interaction>> interactionMap = customInteractionManager.getInteractionsByTokenAndProteins(token, accs);

        return interactionManager.getDetailInteractionResult(interactionMap, "custom");

    }
}
