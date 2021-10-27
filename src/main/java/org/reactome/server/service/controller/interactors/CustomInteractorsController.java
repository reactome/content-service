package org.reactome.server.service.controller.interactors;

import io.swagger.annotations.*;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.model.TupleResult;
import org.reactome.server.service.exception.ErrorInfo;
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
@Api(tags = {"interactors"})
@RequestMapping(value = "/interactors/upload/tuple")
@RestController
public class CustomInteractorsController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;

    @ApiOperation(value = "Parse file and retrieve a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Bad request", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 413, message = "Payload too Large", response = ErrorInfo.class),
            @ApiResponse(code = 415, message = "Unsupported Media Type ('text/plain' or 'text/csv')", response = ErrorInfo.class),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    //todo : name and file parameters are rendered as not required duo to the multipart/form-data
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
    @ResponseBody
    public TupleResult postFile(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                                @RequestParam String name,
                                @ApiParam(name = "file", required = true, value = "Upload your custom interactor file")
                                @RequestPart MultipartFile file) throws IOException, ParserException {
        infoLogger.info("Custom Interaction form request has been submitted");
        return customInteractionManager.getUserDataContainerFromFile(name, file);
    }

    @ApiOperation(value = "Paste file content and get a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Bad request", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 413, message = "Payload too Large", response = ErrorInfo.class),
            @ApiResponse(code = 415, message = "Unsupported Media Type ('text/plain' or 'text/csv')", response = ErrorInfo.class),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/content", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postFileContent(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                                       @RequestParam String name,
                                       @ApiParam(name = "file content", value = "Paste custom interactors file content", required = true)
                                       @RequestBody String fileContent) throws ParserException {
        infoLogger.info("Custom Interaction content request has been submitted");
        return customInteractionManager.getUserDataContainerFromContent(name, fileContent);
    }

    @ApiOperation(value = "Send file via URL and get a summary associated with a token", response = TupleResult.class, produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Bad request", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 415, message = "Unsupported Media Type ('text/plain' or 'text/csv')", response = ErrorInfo.class),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = ErrorInfo.class),
            @ApiResponse(code = 431, message = "Request Header Fields Too Large", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/url", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public TupleResult postUrl(@ApiParam(name = "name", required = true, value = "Name which identifies the sample")
                               @RequestParam String name,
                               @ApiParam(name = "url", required = true, value = "A URL pointing to the Interactors file")
                               @RequestBody String url) throws ParserException {
        infoLogger.info("Custom Interaction url request has been submitted");
        String fileNamefromUrl = customInteractionManager.getFileNameFromURL(url);
        return customInteractionManager.getUserDataContainerFromURL(name, fileNamefromUrl, url);
    }
}
