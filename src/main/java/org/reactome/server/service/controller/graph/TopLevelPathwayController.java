package org.reactome.server.service.controller.graph;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.TopLevelPathway;
import org.reactome.server.graph.service.TopLevelPathwayService;
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
@Api(tags = "topLevelPathways", description = "Reactome Data " )
@RequestMapping("/data")
public class TopLevelPathwayController {

    @Autowired
    private TopLevelPathwayService topLevelPathwayService;

    @ApiOperation(value = "Retrieves all Reactome top level pathways",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathway", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathways()  {
        return topLevelPathwayService.getTopLevelPathways();
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathway/{speciesName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathwaysByName(@ApiParam(defaultValue = "Homo sapiens",required = true) @PathVariable String speciesName)  {
        return topLevelPathwayService.getTopLevelPathways(speciesName);
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species dbId",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathwaysById/{speciesId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathwaysById(@ApiParam(defaultValue = "48887",required = true) @PathVariable Long speciesId)  {
        return topLevelPathwayService.getTopLevelPathways(speciesId);
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species taxonomy id",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/topLevelPathwaysByTaxId/{taxId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getTopLevelPathwaysByTaxId(@ApiParam(defaultValue = "9606",required = true) @PathVariable String taxId)  {
        return topLevelPathwayService.getTopLevelPathwaysByTaxId(taxId);
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/curatedTopLevelPathway", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getCuratedTopLevelPathways()  {
        return topLevelPathwayService.getCuratedTopLevelPathways();
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species name",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/curatedTopLevelPathway/{speciesName}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getCuratedTopLevelPathwaysByName(@ApiParam(defaultValue = "Homo sapiens",required = true) @PathVariable String speciesName)  {
        return topLevelPathwayService.getCuratedTopLevelPathways(speciesName);
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species dbId",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/curatedTopLevelPathwaysById/{speciesId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getCuratedTopLevelPathwaysById(@ApiParam(defaultValue = "48887",required = true) @PathVariable Long speciesId)  {
        return topLevelPathwayService.getCuratedTopLevelPathways(speciesId);
    }

    @ApiOperation(value = "Retrieves all Reactome top level pathways for given species taxonomy id",response = DatabaseObject.class, responseContainer = "List", produces = "application/json")
    @RequestMapping(value = "/curatedTopLevelPathwaysByTaxId/{taxId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Collection<TopLevelPathway> getCuratedTopLevelPathwaysByTaxId(@ApiParam(defaultValue = "9606",required = true) @PathVariable String taxId)  {
        return topLevelPathwayService.getCuratedTopLevelPathwaysByTaxId(taxId);
    }
}
