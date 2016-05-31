package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.domain.model.Publication;
import org.reactome.server.graph.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "person", description = "Reactome Data " )
@RequestMapping("/data")
public class PersonController {

    @Autowired
    private PersonService personService;
    
    @ApiOperation(value = "Retrieves a list of persons where first or lastname equals the given string",response = Person.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/findPersonByName/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> findPersonByName(@ApiParam(defaultValue = "Steve Jupe",required = true) @PathVariable String name) {
        return personService.findPersonByName(name);
    }

    @ApiOperation(value = "Retrieves a list of persons where first or lastname contains the given string",response = Person.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/queryPersonByName/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> queryPersonByName(@ApiParam(defaultValue = "Steve Jupe",required = true) @PathVariable String name) {
        return personService.queryPersonByName(name);
    }

    @ApiOperation(value = "Retrieves a person for given Orcid identifier",response = Person.class, produces = "application/json")
    @RequestMapping(value = "/findPersonByOrcidId/{orcidId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Person findPersonByOrcidId(@ApiParam(defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String orcidId) {
        return personService.findPersonByOrcidId(orcidId);
    }

    @ApiOperation(value = "Retrieves a person for given dbId",response = Person.class, produces = "application/json")
    @RequestMapping(value = "/findPersonByDbId/{dbId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Person findPersonByDbId(@ApiParam(defaultValue = "391309",required = true) @PathVariable Long dbId) {
        return personService.findPersonByDbId(dbId);
    }

    @ApiOperation(value = "Retrieves a list of publication of a person identified by their Orcid identifier",response = Publication.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/getPublicationsOfPersonByOrcidId/{orcidId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Publication> getPublicationsOfPersonByOrcidId(@ApiParam(defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String orcidId) {
        return personService.getPublicationsOfPersonByOrcidId(orcidId);
    }

    @ApiOperation(value = "Retrieves a list of publication of a person identified by their dbId",response = Publication.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/getPublicationsOfPersonByDbId/{dbId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Publication> getPublicationsOfPersonByDbId(@ApiParam(defaultValue = "391309",required = true) @PathVariable Long dbId) {
        return personService.getPublicationsOfPersonByDbId(dbId);
    }


    @ApiOperation(value = "Retrieves a list of pathways authored by a person identified by their Orcid identifier",response = Publication.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/getAuthoredPathwaysByOrcidId/{orcidId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getAuthoredPathwaysByOrcidId(@ApiParam(defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String orcidId) {
        return personService.getAuthoredPathwaysByOrcidId(orcidId);
    }

    @ApiOperation(value = "Retrieves a list of pathways authored by a person identified by their dbId",response = Publication.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/getAuthoredPathwaysByDbId/{dbId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getAuthoredPathwaysByDbId(@ApiParam(defaultValue = "391309",required = true) @PathVariable Long dbId) {
        return personService.getAuthoredPathwaysByDbId(dbId);
    }

}
