package org.reactome.server.service.controller.interactors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.model.TupleResult;
import org.reactome.server.service.manager.CustomInteractorManager;
import org.reactome.server.service.manager.InteractionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@Tag(name = "interactors")
@RequestMapping(value = "/interactors/upload/tuple")
@RestController
public class CustomInteractorsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;

    @Operation(summary = "Parse file and retrieve a summary associated with a token")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "413", description = "Payload too Large"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type ('text/plain' or 'text/csv')"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    //todo : name and file parameters are rendered as not required duo to the multipart/form-data
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
    @ResponseBody
    public TupleResult postFile(@Parameter(name = "name", required = true, description = "Name which identifies the sample")
                                @RequestParam String name,
                                @Parameter(name = "file", required = true, description = "Upload your custom interactor file")
                                @RequestPart MultipartFile file) throws IOException, ParserException {
        infoLogger.info("Custom Interaction form request has been submitted");
        return customInteractionManager.getUserDataContainerFromFile(name, file);
    }

    @Operation(summary = "Paste file content and get a summary associated with a token")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "413", description = "Payload too Large"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type ('text/plain' or 'text/csv')"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postFileContent(@Parameter(name = "name", required = true, description = "Name which identifies the sample")
                                       @RequestParam String name,
                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "<b>File Content</b>Paste custom interactors file content", required = true)
                                       @RequestBody String fileContent) throws ParserException {
        infoLogger.info("Custom Interaction content request has been submitted");
        return customInteractionManager.getUserDataContainerFromContent(name, fileContent);
    }

    @Operation(summary = "Send file via URL and get a summary associated with a token")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type ('text/plain' or 'text/csv')"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
            @ApiResponse(responseCode = "431", description = "Request Header Fields Too Large"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postUrl(@Parameter(name = "name", required = true, description = "Name which identifies the sample")
                               @RequestParam String name,
                               @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "<b>url</b> A URL pointing to the Interactors file", required = true)
                               @RequestBody String url) throws ParserException {
        infoLogger.info("Custom Interaction url request has been submitted");
        String fileNamefromUrl = customInteractionManager.getFileNameFromURL(url);
        return customInteractionManager.getUserDataContainerFromURL(name, fileNamefromUrl, url);
    }
}
