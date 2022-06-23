package org.reactome.server.service.controller.exporter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.batik.transcoder.TranscoderException;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.FireworksExporterException;
import org.reactome.server.service.manager.SearchManager;
import org.reactome.server.tools.fireworks.exporter.FireworksExporter;
import org.reactome.server.tools.fireworks.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.fireworks.exporter.common.api.FireworkArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 * @author Pascual Lorente (plorente@ebi.ac.uk)
 */
@RestController
@Tag(name = "exporter", description = "Reactome Data: Format Exporter")
@RequestMapping("/exporter")
public class FireworksImageExporterController {

    private FireworksExporter fireworksExporter;

    private SpeciesService speciesService;
    private SearchManager searchManager;

    @Operation(
            summary = "Exports a given pathway overview to the specified image format (png, jpg, jpeg, svg, gif)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/png"),
                    @Content(mediaType = "image/jpg"),
                    @Content(mediaType = "image/jpeg"),
                    @Content(mediaType = "image/svg+xml"),
                    @Content(mediaType = "image/gif"),
            }),
            @ApiResponse(responseCode = "404", description = "Species does not match with any of the available."),
            @ApiResponse(responseCode = "500", description = "Could not deserialize pathways overview file."),
    })
    @RequestMapping(value = "/fireworks/{species}.{ext:.*}", method = RequestMethod.GET, produces = {"image/png", "image/jpg", "image/jpeg", "image/svg+xml", "image/gif"})
    public void diagramImage(@Parameter(description = "Species identifier (it can be the taxonomy id, species name or dbId)", required = true, example = "9606")
                             @PathVariable String species,
                             @Parameter(description = "File extension (defines the image format)", required = true, example = "svg", schema = @Schema(allowableValues = {"png", "jpg", "jpeg", "svg", "gif"}))
                             @PathVariable String ext,
                             @Parameter(description = "Result image quality between [1 - 10]. It defines the quality of the final image (Default 5)", example = "5")
                             @RequestParam(value = "quality", required = false, defaultValue = "5") Integer quality,
                             @Parameter(description = "Gene name, protein or chemical identifier or Reactome identifier used to flag elements in the diagram")
                             @RequestParam(value = "flg", required = false) String flg,
                             @Parameter(description = "Defines whether to take into account interactors for the flagging")
                             @RequestParam(value = "flgInteractors", required = false, defaultValue = "true") Boolean flgInteractors,
                             @Parameter(description = "Highlight element(s) selection in the diagram. CSV line.")
                             @RequestParam(value = "sel", required = false) List<String> sel,
                             @Parameter(description = "Sets whether the name of the pathway is shown below", example = "true")
                             @RequestParam(value = "title", required = false, defaultValue = "true") Boolean title,
                             @Parameter(description = "Defines the image margin between [0 - 20] (Default 15)", example = "15")
                             @RequestParam(value = "margin", defaultValue = "15", required = false) Integer margin,

                             @Parameter(description = "Diagram Color Profile", example = "Copper",schema = @Schema(allowableValues = {"Copper", "Copper plus", "Barium Lithium", "Calcium Salts"}))
                             @RequestParam(value = "diagramProfile", defaultValue = "Copper", required = false) String profile,
                             @Parameter(description = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> token with the results to be overlaid on top of the given pathways overview")
                             @RequestParam(value = "token", required = false) String token,
                             @Parameter(description = "The <a href=\"/dev/analysis\" target=\"_blank\">analysis</a> resource for which the results will be overlaid on top of the given pathways overview")
                             @RequestParam(value = "resource", required = false, defaultValue = "TOTAL") String resource,
                             @Parameter(description = "Expression column. When the token is associated to an expression analysis, this parameter allows specifying the expression column for the overlay")
                             @RequestParam(value = "expColumn", required = false) Integer expColumn,
                             @Parameter(description = "Set to 'true' to overlay analysis coverage values")
                             @RequestParam(value = "coverage", required = false, defaultValue = "false") Boolean coverage,

                             HttpServletResponse response) {

        Species s = speciesService.getSpecies(species);
        if (s == null) throw new FireworksExporterException(String.format("'%s' is not a species", species));

        //NO PDF for the time being
        if (ext.equalsIgnoreCase("pdf")) throw new IllegalArgumentException("Unsupported file extension pdf");

        FireworkArgs args = new FireworkArgs(s.getDisplayName().replace(" ", "_"), ext);
        args.setSelected(sel);
        args.setProfile(profile);
        args.setWriteTitle(title);
        args.setQuality(quality);
        args.setMargin(margin);
        args.setToken(token);
        args.setResource(resource);
        args.setColumn(expColumn);
        args.setCoverage(coverage);

        if (flg != null && !flg.isEmpty()) {
            try {
                args.setFlags(searchManager.getFireworksFlagging(s, flg, flgInteractors));
            } catch (SolrSearcherException e) {
                //Nothing to be flagged
            }
        }

        try {
            String type = ext.equalsIgnoreCase("svg") ? "svg+xml" : ext.toLowerCase();
            response.addHeader("Content-Type", "image/" + type);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + s.getDisplayName() + "." + ext + "\"");
            fireworksExporter.render(args, response.getOutputStream());
        } catch (IOException | AnalysisServerError | TranscoderException e) {
            e.printStackTrace();
        }

    }

    @Autowired
    public void setFireworksExporter(FireworksExporter fireworksExporter) {
        this.fireworksExporter = fireworksExporter;
    }

    @Autowired
    public void setSpeciesService(SpeciesService speciesService) {
        this.speciesService = speciesService;
    }

    @Autowired
    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }
}
