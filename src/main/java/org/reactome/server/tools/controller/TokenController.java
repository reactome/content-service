package org.reactome.server.tools.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.tuple.model.CustomInteractorRepository;
import org.reactome.server.tools.interactors.tuple.model.CustomPsicquicRepository;
import org.reactome.server.tools.manager.CustomInteractorManager;
import org.reactome.server.tools.manager.InteractionManager;
import org.reactome.server.tools.model.interactors.Interactors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Api(tags = "interactors", description = "Molecule interactors")
@RequestMapping(value = "/interactors/token")
@RestController
public class TokenController {

    private static final String CUSTOM_RESOURCE_NAME = "custom";

    @Autowired
    public CustomInteractorManager customInteractionManager;

    @Autowired
    public InteractionManager interactionManager;

    @ApiOperation(value = "Retrieve custom interactions associated with a token", response = Interactors.class, produces = "application/json")
    @RequestMapping(value = "/{token}", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody
    public Interactors getInteractors(@ApiParam(value = "A token associated with a data submission", required = true)
                                      @PathVariable String token,
                                      @ApiParam(value = "Interactors accessions", required = true)
                                      @RequestBody String proteins) {

        /** Split param and put into a Set to avoid duplicates **/
        Set<String> accs = new HashSet<>(Arrays.asList(proteins.split("\\s*,\\s*")));

        Map<String, List<Interaction>> interactionMap = customInteractionManager.getInteractionsByTokenAndProteins(token, accs);

        return interactionManager.getCustomInteractionResult(interactionMap, CUSTOM_RESOURCE_NAME, token);
    }

    @RequestMapping(value = "/token/listall", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Deprecated
    public Set<String> listAllTokens() {
        return CustomInteractorRepository.getKeys();
    }

    @RequestMapping(value = "/token/listallpsicq", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Deprecated
    public Set<String> listAllTokensPsicquic() {
        return CustomPsicquicRepository.getKeys();
    }

    @RequestMapping(value = "/token/listpsicquicrepo", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Deprecated
    public Map<String, String> listPsicquicRepo() {
        return CustomPsicquicRepository.getAll();
    }
}
