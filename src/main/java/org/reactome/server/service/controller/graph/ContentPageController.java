package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.service.manager.ContentPageManager;
import org.reactome.server.service.model.content.ContributorResponse;
import org.reactome.server.service.model.content.DoiPathwayResponse;
import org.reactome.server.service.model.content.TocPathwayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@Tag(name = "content", description = "Reactome Data: Content page queries (TOC, DOI, Contributors)")
@RequestMapping("/data")
public class ContentPageController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private ContentPageManager contentPageManager;

    @Operation(
            summary = "Table of Contents pathways",
            description = "Returns all top-level pathways with authors, reviewers, editors, and sub-pathways for the Table of Contents page."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/content/toc", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<TocPathwayResponse> getTocPathways() {
        infoLogger.info("Request for TOC pathways");
        return contentPageManager.getTocPathways();
    }

    @Operation(
            summary = "DOI pathways",
            description = "Returns all pathways with DOI assignments, including authors, reviewers, and editors."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/content/doi", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<DoiPathwayResponse> getDoiPathways() {
        infoLogger.info("Request for DOI pathways");
        return contentPageManager.getDoiPathways();
    }

    @Operation(
            summary = "Contributors",
            description = "Returns all contributors with counts of authored and reviewed pathways and reactions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/content/contributors", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ContributorResponse> getContributors() {
        infoLogger.info("Request for contributors");
        return contentPageManager.getContributors();
    }
}
