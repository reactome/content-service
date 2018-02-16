package org.reactome.server.service.manager;

import org.apache.batik.transcoder.TranscoderException;
import org.reactome.server.service.exception.DiagramExporterException;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.raster.RasterExporter;
import org.reactome.server.tools.diagram.exporter.raster.RasterOutput;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Lorente-Arencibia, Pascual (plorente@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@Component
public class DiagramRasterExportManager {

    private final RasterExporter rasterExporter;

    @Autowired
    public DiagramRasterExportManager(RasterExporter rasterExporter) {
        this.rasterExporter = rasterExporter;
    }

    public void exportRaster(RasterArgs args, HttpServletResponse response) throws TranscoderException, DiagramJsonDeserializationException, AnalysisException, EhldException, DiagramJsonNotFoundException {

        final String ext = args.getFormat();
        final Integer column = args.getColumn();
        // TODO: return codes through response
        // - token not valid
        // - unknown stable identifier
        // - invalid format/ext

        //response.addHeader("Content-Disposition", "attachment; filename=\"" + args.getStId() + "." + args.getFormat().toLowerCase() + "\"");
        String type = args.getFormat().equalsIgnoreCase("svg") ? "svg+xml" : args.getFormat().toLowerCase();
        response.addHeader("Content-Type", "image/" + type);
        if (ext.equalsIgnoreCase("gif")) {
            if (column == null) {
                gif(args, response);
            } else {
                normal(args, response);
            }
        } else if (ext.equalsIgnoreCase("svg")) {
            svg(args, response);
        } else {
            normal(args, response);
        }
    }

    private void gif(RasterArgs args, HttpServletResponse response) throws DiagramJsonDeserializationException, AnalysisException, EhldException, DiagramJsonNotFoundException {
        try {
            OutputStream os = response.getOutputStream();
            rasterExporter.exportToGif(args, os);
        } catch (IOException e) {
            throw new DiagramExporterException(e.getMessage(), e);
        }
    }

    private void normal(RasterArgs args, HttpServletResponse response) throws DiagramJsonDeserializationException, AnalysisException, EhldException, DiagramJsonNotFoundException {
        try {
            OutputStream os = response.getOutputStream();
            final BufferedImage image = rasterExporter.export(args);
            ImageIO.write(image, args.getFormat(), os);
        } catch (IOException e) {
            throw new DiagramExporterException(e.getMessage(), e);
        }
    }

    private void svg(RasterArgs args, HttpServletResponse response) throws TranscoderException, DiagramJsonDeserializationException, AnalysisException, EhldException, DiagramJsonNotFoundException {
        try {
            OutputStream os = response.getOutputStream();
            SVGDocument svg = rasterExporter.exportToSvg(args);
            RasterOutput.save(svg, os);
        } catch (IOException e) {
            throw new DiagramExporterException(e.getMessage(), e);
        }
    }
}
