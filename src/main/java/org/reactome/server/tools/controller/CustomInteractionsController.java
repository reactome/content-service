package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.tuple.model.Summary;
import org.reactome.server.tools.manager.CustomInteractionManager;
import org.reactome.server.tools.manager.InteractionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */


@Api(value = "interactors", description = "Tuple Overlay Controller")
@RequestMapping(value = "/interactors/upload/tuple")
@RestController
public class CustomInteractionsController {

    @Autowired
    public CustomInteractionManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;


    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Summary getPostFile(@ApiParam(name = "file", required = true, value = "A file with the data to be analysed")
                               @RequestParam(required = true) MultipartFile file) throws IOException {

        Summary summary = customInteractionManager.getUserDataContainer(file.getInputStream());

        /// STORE AND RETURN A TOKEN
        customInteractionManager.saveToken(summary);

        return summary;
    }

    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public Summary getPostFileContent(@ApiParam(name = "file content", value = "The file content with data to be analysed", required = true)
                                      @RequestBody String fileContent) {

        Summary summary = customInteractionManager.getUserDataContainer(fileContent);

        /// STORE AND RETURN A TOKEN
        customInteractionManager.saveToken(summary);

        return summary;
    }

    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public Summary getPostUrl(@ApiParam(name = "url", required = true, value = "A URL pointing to the Interactors file")
                              @RequestBody String url) {

        Summary summary = customInteractionManager.getUserDataContainerFromURL(url);


        /// STORE AND RETURN A TOKEN
        customInteractionManager.saveToken(summary);

        return summary;

    }
}
