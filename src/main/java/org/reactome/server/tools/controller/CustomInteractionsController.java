package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.tuple.model.Summary;
import org.reactome.server.tools.interactors.tuple.token.Token;
import org.reactome.server.tools.manager.CustomInteractionManager;
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


@Api(tags = "tuple", description = "Tuple Overlay Controller")
@RequestMapping(value = "/tuple")
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

    @RequestMapping(value = "/file", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
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

    @RequestMapping(value = "/token/listall", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Token> listAllTokens() {
        List<Token> tokens = new ArrayList<>();

        Map<Token, Summary> all = CustomInteractionManager.tokenMap;
        for (Token token : all.keySet()) {
            tokens.add(token);
        }

        return tokens;
    }
}
