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
import springfox.documentation.annotations.ApiIgnore;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Api(value = "interactors", description = "Tuple Overlay Controller")
@RequestMapping(value = "/interactors/token")
@RestController
public class CustomInteractors {

    @Autowired
    public CustomInteractionManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;

    @RequestMapping(value = "/{token}", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
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

    @ApiIgnore
    @RequestMapping(value = "/listall", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Deprecated
    public List<Token> listAllTokens() {
        List<Token> tokens = new ArrayList<>();

        Map<Token, Summary> all = CustomInteractionManager.tokenMap;
        for (Token token : all.keySet()) {
            tokens.add(token);
        }

        return tokens;
    }
}
