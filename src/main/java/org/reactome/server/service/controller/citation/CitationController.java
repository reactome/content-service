package org.reactome.server.service.controller.citation;

import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.InstanceEdit;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Person;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




/**
 * @author Yusra Haider (yhaiderr@ebi.ac.uk)
 * @since 11.02.2
 */

@ApiIgnore
@RestController
@RequestMapping("/citation")
public class CitationController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;
    @Autowired
    private GeneralService generalService;

    @GetMapping(value = "/pathway/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> pathwayCitation(@ApiParam(value = "DbId or StId of the requested database object", required = true)
                                             @PathVariable String id) {

        DatabaseObject databaseObject = advancedDatabaseObjectService.findEnhancedObjectById(id);
        Map<String, Object> map = new HashMap<>();
        if (databaseObject instanceof Pathway) {
            Pathway p = (Pathway)databaseObject;
            map.put("publicationYear", p.getReleaseDate().substring(0,4));
            map.put("doi", p.getDoi());
            map.put("pathwayTitle", p.getDisplayName());
            map.put("hasImage", p.getHasDiagram());

            List<String> authors = null;
            List<InstanceEdit> instanceEdits = null;

            if (p.getAuthored() != null && !p.getAuthored().isEmpty()) {
                instanceEdits = p.getAuthored();
            }
            else if (p.getCreated() != null) {
                instanceEdits = new ArrayList<>();
                instanceEdits.add(p.getCreated());
            }
            else if (p.getReviewed() != null && !p.getReviewed().isEmpty()) {
                instanceEdits = p.getReviewed();
            }

            if (instanceEdits != null) {
                authors = new ArrayList<>();
                for(InstanceEdit instanceEdit: instanceEdits){
                    for(Person person: instanceEdit.getAuthor()){
                        authors.add(person.getSurname()  + ", " + String.join(".", person.getInitial().split("")) + ".");
                    }
                }
            }

            map.put("authors", authors);
            map.put("releaseVersion", generalService.getDBInfo().getVersion());
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping(value = "/download", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> downloadCitation() {
        String downloadLink = "https://reactome.org/download-data/";
        return ResponseEntity.ok("\"Name of file\", Reactome, " + generalService.getDBInfo().getVersion() + ", " + downloadLink);
    }
}
