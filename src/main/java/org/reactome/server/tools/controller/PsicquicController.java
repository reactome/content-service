package org.reactome.server.tools.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@RestController
@RequestMapping("/interaction")
public class PsicquicController {

    @RequestMapping(value = "/resources", method = RequestMethod.GET)
    public @ResponseBody String resources()  {

        return "{msg:resource...to be created}";
    }

}
