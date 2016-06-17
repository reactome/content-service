package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.domain.model.Publication;
import org.reactome.server.graph.service.PersonService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@RestController
@Api(tags = "person", description = "Reactome Data: Person queries" )
@RequestMapping("/data")
public class PersonController {

    @Autowired
    private PersonService personService;

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A list of persons where first or lastname equals the given string")
    @RequestMapping(value = "/people/name/{name}/exact", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> findPersonByName(@ApiParam(defaultValue = "Steve Jupe",required = true) @PathVariable String name) {
        Collection<Person> persons = personService.findPersonByName(name);
        if (persons == null || persons.isEmpty())  throw new NotFoundException("No persons found for name: " + name);
        return persons;
    }

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A list of persons where first or lastname contains the given string")
    @RequestMapping(value = "/people/name/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> queryPersonByName(@ApiParam(defaultValue = "Steve Jupe",required = true) @PathVariable String name) {
        Collection<Person> persons = personService.queryPersonByName(name);
        if (persons == null || persons.isEmpty())  throw new NotFoundException("No persons found for name: " + name);
        return persons;
    }

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A person for given identifier", notes = "Person identifier: Can be OrcidId or DbId")
    @RequestMapping(value = "/person/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Person findPerson(@ApiParam(value = "Person identifier: Can be OrcidId or DbId", defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String id) {
        Person person = personService.findPerson(id);
        if (person == null)  throw new NotFoundException("No person found for id: " + id);
        return person;
    }

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A person for given identifier", notes = "Person identifier: Can be OrcidId or DbId. Retrieves a single property from the list of DatabaseObjects")
    @RequestMapping(value = "/person/{id}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findPerson(@ApiParam(value = "Person identifier: Can be OrcidId or DbId", defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String id,
                             @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Person person = personService.findPerson(id);
        if (person == null)  throw new NotFoundException("No person found for id: " + id);
        return ControllerUtils.getProperty(person, attributeName);
    }

    @ApiOperation(value = "A list of publication of a person", notes = "Person identifier: Can be OrcidId or DbId")
    @RequestMapping(value = "/person/{id}/publications", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Publication> getPublicationsOfPerson(@ApiParam(value = "Person identifier: Can be OrcidId, DbId or Email", defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String id) {
        Collection<Publication> publications = personService.getPublicationsOfPerson(id);
        if (publications == null || publications.isEmpty())  throw new NotFoundException("No publications found for person with id: " + id);
        return publications;
    }

    @ApiOperation(value = "A list of pathways authored by a person", notes = "Person identifier: Can be OrcidId or DbId")
    @RequestMapping(value = "/person/{id}/authoredPathways", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getAuthoredPathways(@ApiParam(value = "Person identifier: Can be OrcidId, DbId or Email", defaultValue = "0000-0001-5807-0069",required = true) @PathVariable String id) {
        Collection<Pathway> pathways = personService.getAuthoredPathways(id);
        if (pathways == null || pathways.isEmpty())  throw new NotFoundException("No pathways found for person with id: " + id);
        return pathways;
    }
}
