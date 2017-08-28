package org.reactome.server.service.controller.graph;

import io.swagger.annotations.*;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.domain.model.Publication;
import org.reactome.server.graph.service.PersonService;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.ErrorInfo;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.NotFoundTextPlainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@SuppressWarnings("unused")
@RestController
@Api(tags = "person", description = "Reactome Data: Person queries")
@RequestMapping("/data")
public class PersonController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private PersonService personService;

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A list of people with first or last name exactly matching a given string", notes = "Retrieves a list of people in Reactome with either their first or last name matching exactly the given string.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Given name does not exactly match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/people/name/{name}/exact", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> findPersonByName(@ApiParam(value = "Person's first or last name", defaultValue = "Steve Jupe", required = true) @PathVariable String name) {
        Collection<Person> persons = personService.findPersonByName(name);
        if (persons == null || persons.isEmpty()) throw new NotFoundException("No persons found for name: " + name);
        infoLogger.info("Request for person with name: {}", name);
        return persons;
    }

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A list of people with first or last name partly matching a given string", notes = "Retrieves a list of people in Reactome with either their first or last name partly matching the given string.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Given name does not partly match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/people/name/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Person> queryPersonByName(@ApiParam(value = "Person's first or last name", defaultValue = "Steve Jupe", required = true) @PathVariable String name) {
        Collection<Person> persons = personService.queryPersonByName(name);
        if (persons == null || persons.isEmpty()) throw new NotFoundException("No persons found for name: " + name);
        infoLogger.info("Request for person with name: {}", name);
        return persons;
    }

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A person by his/her identifier", notes = "Retrieves a person in Reactome by his/her OrcidId or DbId.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "OrcidId or DbId does not match with any in current data", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/person/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Person findPerson(@ApiParam(value = "Person identifier: Can be OrcidId or DbId", defaultValue = "0000-0001-5807-0069", required = true) @PathVariable String id) {
        Person person = personService.findPerson(id);
        if (person == null) throw new NotFoundException("No person found for id: " + id);
        infoLogger.info("Request for person with id: {}", id);
        return person;
    }

    //ToDo in regards to data privacy, should this not be removed or restricted? emails have been removed, is this enough
    @ApiOperation(value = "A person's property by his/her identifier", notes = "Retrieves a specific person's property by his/her OrcidId or DbId.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "OrcidId or DbId does not match with any in current data or invalid attribute name", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/person/{id}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findPerson(@ApiParam(value = "Person identifier: Can be OrcidId or DbId", defaultValue = "0000-0001-5807-0069", required = true) @PathVariable String id,
                             @ApiParam(value = "Attribute to be filtered", defaultValue = "displayName", required = true) @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        Person person = personService.findPerson(id);
        if (person == null) throw new NotFoundTextPlainException("No person found for id: " + id);
        infoLogger.info("Request for person with id: {}", id);
        return ControllerUtils.getProperty(person, attributeName);
    }

    @ApiOperation(value = "A list of publications authored by a given person", notes = "Retrieves a list of publications authored by a given person. OrcidId, DbId or Email can be used to specify the person.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "OrcidId or DbId does not match with any publication", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/person/{id}/publications", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Publication> getPublicationsOfPerson(@ApiParam(value = "Person identifier: Can be OrcidId, DbId or Email", defaultValue = "0000-0001-5807-0069", required = true) @PathVariable String id) {
        Collection<Publication> publications = personService.getPublicationsOfPerson(id);
        infoLogger.info("Request for all publications of person with id: {}", id);
        return publications;
    }

    @ApiOperation(value = "A list of pathways authored by a given person", notes = "Retrieves a list of pathways authored by a given person. OrcidId, DbId or Email can be used to specify the person.")
    @ApiResponses({
            @ApiResponse(code = 404, message = "OrcidId or DbId does not retrieve any pathway", response = ErrorInfo.class),
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/person/{id}/authoredPathways", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<Pathway> getAuthoredPathways(@ApiParam(value = "Person identifier: Can be OrcidId, DbId or Email", defaultValue = "0000-0001-5807-0069", required = true) @PathVariable String id) {
        Collection<Pathway> pathways = personService.getAuthoredPathways(id);
        infoLogger.info("Request for all authored pathways of person with id: {}", id);
        return pathways;
    }
}
