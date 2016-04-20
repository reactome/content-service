package org.reactome.server.controller.interactors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.tuple.model.TupleResult;
import org.reactome.server.manager.CustomInteractorManager;
import org.reactome.server.manager.InteractionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
    @ResponseBody
    public TupleResult postFile(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                                @RequestParam(required = true) String name,
                                @ApiParam(name = "file", required = true, value = "Upload your custom interactor file")
                                @RequestPart(required = true) MultipartFile file) throws IOException {

        return customInteractionManager.getUserDataContainerFromFile(name, file);
    }

    @ApiOperation(value = "Paste file content and get a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postFileContent(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                                       @RequestParam(required = true) String name,
                                       @ApiParam(name = "file content", value = "Paste custom interactors file content", required = true)
                                       @RequestBody String fileContent) {

        return customInteractionManager.getUserDataContainerFromContent(name, fileContent);
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
}
