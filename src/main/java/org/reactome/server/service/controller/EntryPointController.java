package org.reactome.server.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@Controller
public class EntryPointController {

    @RequestMapping(value = {"/", "/index.html"}, method = RequestMethod.GET)
    @ApiIgnore //Swagger will NOT include this method in the documentation
    public String entryPoint () {
        return "index";
    }

}