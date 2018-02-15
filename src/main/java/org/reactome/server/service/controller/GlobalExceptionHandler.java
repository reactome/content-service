package org.reactome.server.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.catalina.connector.ClientAbortException;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.neo4j.ogm.exception.ConnectionException;
import org.reactome.server.interactors.exception.CustomPsicquicInteractionClusterException;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.exception.PsicquicResourceNotFoundException;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.exception.TupleParserException;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.*;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import psidev.psi.mi.tab.PsimiTabException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;

/**
 * Global exception handler controller
 * This controller will deal with all exceptions thrown by the other controllers if they don't treat them individually
 * <p>
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@ControllerAdvice
@SuppressWarnings("unused")
class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger("errorLogger");
    private static final Logger onlyEmailLogger = LoggerFactory.getLogger("onlyEmailLogger");

    //================================================================================
    // NotFound
    //================================================================================

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleNotFoundException(HttpServletRequest request, NotFoundException e) {
        //no logging here!
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundTextPlainException.class)
    @ResponseBody
    ResponseEntity<String> handleNotFoundTextPlainException(HttpServletRequest request, NotFoundTextPlainException e) {
        //no logging here!
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    //================================================================================
    // SOLR
    //================================================================================

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SolrSearcherException.class)
    @ResponseBody
    ResponseEntity<String> handleSolrException(HttpServletRequest request, SolrSearcherException e) {
        logger.error("Solr exception was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    //================================================================================
    // Interactors
    //================================================================================

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StaticInteractionException.class)
    @ResponseBody
    ResponseEntity<String> handleStaticInteractionException(HttpServletRequest request, StaticInteractionException e) {
        logger.warn("StaticInteractionException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicContentException.class)
    @ResponseBody
    ResponseEntity<String> handlePsicquicContentException(HttpServletRequest request, PsicquicContentException e) {
        logger.warn("PsicquicContentException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicQueryException.class)
    @ResponseBody
    ResponseEntity<String> handlePsicquicQueryException(HttpServletRequest request, PsicquicQueryException e) {
        logger.warn("PsicquicQueryException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "PSICQUIC resource is not responding");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicResourceNotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handlePsicquicResourceNotFoundException(HttpServletRequest request, PsicquicResourceNotFoundException e) {
        logger.warn("PsicquicResourceNotFoundException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsimiTabException.class)
    @ResponseBody
    ResponseEntity<String> handlePsimiTabException(HttpServletRequest request, PsimiTabException e) {
        logger.warn("PsimiTabException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Couldn't parse PSICQUIC result");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicRegistryClientException.class)
    @ResponseBody
    ResponseEntity<String> handlePsicquicRegistryClientException(HttpServletRequest request, PsicquicRegistryClientException e) {
        logger.warn("PsicquicRegistryClientException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Couldn't query PSICQUIC Resources");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomPsicquicInteractionClusterException.class)
    @ResponseBody
    ResponseEntity<String> handleCustomPsicquicInteractionClusterException(HttpServletRequest request, CustomPsicquicInteractionClusterException e) {
        logger.warn("CustomPsicquicInteractionClusterException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Error querying your PSICQUIC Resource.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TupleParserException.class)
    @ResponseBody
    ErrorInfo handleTupleParserException(HttpServletRequest request, TupleParserException e) {
        logger.warn("TupleParserException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, request.getRequestURL(), e.getErrorMessages().toArray(new String[e.getErrorMessages().size()]));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ParserException.class)
    @ResponseBody
    ResponseEntity<String> handleParserException(HttpServletRequest request, ParserException e) {
        logger.warn("ParserException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InteractorResourceNotFound.class)
    @ResponseBody
    ResponseEntity<String> handleInteractorResourceNotFound(HttpServletRequest request, InteractorResourceNotFound e) {
        logger.warn("InteractorResourceNotFound was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
    @ExceptionHandler(RequestEntityTooLargeException.class)
    @ResponseBody
    ResponseEntity<String> handleRequestEntityTooLargeException(HttpServletRequest request, RequestEntityTooLargeException e) {
        logger.warn("RequestEntityTooLargeException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseBody
    ResponseEntity<String> handleUnprocessableEntityException(HttpServletRequest request, UnprocessableEntityException e) {
        logger.warn("UnprocessableEntityException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TokenNotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleTokenNotFoundException(HttpServletRequest request, TokenNotFoundException e) {
        logger.warn("TokenNotFoundException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(UnsupportedMediaTypeException.class)
    @ResponseBody
    ResponseEntity<String> handleUnsupportedMediaTypeException(HttpServletRequest request, UnsupportedMediaTypeException e) {
        logger.warn("UnsupportedMediaTypeException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    ResponseEntity<String> handleMaxUploadSizeExceededException(HttpServletRequest request, MaxUploadSizeExceededException e) {
        logger.warn("UnsupportedMediaTypeException was caught for request: " + request.getRequestURL());
        String msg = "Maximum upload size of " + e.getMaxUploadSize() + " bytes exceeded";
        return toJsonResponse(HttpStatus.PAYLOAD_TOO_LARGE, request, e.getMessage());
    }

    //================================================================================
    // Neo4j
    //================================================================================
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ConnectionException.class)
    @ResponseBody
    ResponseEntity<String> handleNeo4jConnectionException(HttpServletRequest request, ConnectionException e) {
        logger.error("Neo4j ConnectionException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    //================================================================================
    // Diagram Exporter
    //================================================================================

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DiagramJsonNotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleDiagramJsonNotFoundException(HttpServletRequest request, DiagramJsonNotFoundException e) {
        logger.warn("DiagramJsonNotFoundException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DiagramJsonDeserializationException.class)
    @ResponseBody
    ResponseEntity<String> handleDiagramJsonDeserializationException(HttpServletRequest request, DiagramJsonDeserializationException e) {
        logger.warn("DiagramJsonDeserializationException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DiagramProfileException.class)
    @ResponseBody
    ResponseEntity<String> handleDiagramProfileException(HttpServletRequest request, DiagramProfileException e) {
        logger.warn("DiagramProfileException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(AnalysisException.class)
    @ResponseBody
    ResponseEntity<String> handleAnalysisException(HttpServletRequest request, AnalysisException e) {
        logger.warn("AnalysisException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(EHLDException.class)
    @ResponseBody
    ResponseEntity<String> handleEHLDException(HttpServletRequest request, EHLDException e) {
        logger.warn("EHLDException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(TranscoderException.class)
    @ResponseBody
    ResponseEntity<String> handleTranscoderException(HttpServletRequest request, TranscoderException e) {
        logger.warn("SVG TranscoderException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    //================================================================================
    // Default
    //================================================================================

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CertificateException.class)
    @ResponseBody
    ResponseEntity<String> handleCertificateException(HttpServletRequest request, CertificateException e) {
        logger.error("CertificateException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({InvocationTargetException.class, IllegalAccessException.class})
    @ResponseBody
    ResponseEntity<String> handleReflectionError(HttpServletRequest request, Exception e) {
        logger.error("ReflectionException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ClassNotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleClassNotFoundException(HttpServletRequest request, ClassNotFoundException e) {
        logger.warn("ClassNotFoundException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Specified class was not found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        logger.warn("HttpRequestMethodNotSupportedException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
        logger.warn("HttpMediaTypeNotSupportedException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpMediaTypeNotAcceptableException(HttpServletRequest request, HttpMediaTypeNotAcceptableException e) {
        logger.warn("HttpMediaTypeNotSupportedException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.NOT_ACCEPTABLE, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    ResponseEntity<String> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
        logger.warn("MissingServletRequestParameterException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        logger.warn("HttpMessageNotReadableException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpRequestException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpRequestException(HttpServletRequest request, HttpRequestException e) {
        logger.warn("HttpRequestException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Cannot connect to Neo4j Server. Please contact Reactome at help@reactome.org.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    ResponseEntity<String> handleClientAbortException(HttpServletRequest request, ClientAbortException e) {
        // Wrap an IOException identifying it as being caused by an abort of a request by a remote client.
        logger.warn("ClientAbortException was caught, we can ignore it");
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DiagramExporterException.class)
    @ResponseBody
    ResponseEntity<String> handleRasterException(HttpServletRequest request, DiagramExporterException e) {
        logger.warn("DiagramExporterException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MissingSBMLException.class)
    @ResponseBody
    ResponseEntity<String> handleMissingSBMLException(HttpServletRequest request, MissingSBMLException e) {
        logger.error("MissingSBMLException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ResponseEntity<String> handleUnclassified(HttpServletRequest request, Exception e) {
        logger.error("An unspecified exception was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    ResponseEntity<String> handleNullPointerException(HttpServletRequest request, NullPointerException e) {
        onlyEmailLogger.error("NullPointerException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Something unexpected happened and the error has been reported.");
    }

    /*
     * Adding a JSON String manually to the response.
     *
     * Some services return a binary file or text/plain, etc. Then an ErrorInfo instance is manually converted
     * to JSON and written down in the response body.
     */
    private ResponseEntity<String> toJsonResponse(HttpStatus status, HttpServletRequest request, String exceptionMessage) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json");
        try {
            StringBuffer requestURL = (request == null) ? new StringBuffer("") : request.getRequestURL();
            ObjectMapper mapper = new ObjectMapper();
            return ResponseEntity.status(status)
                    .headers(responseHeaders)
                    .body(mapper.writeValueAsString(new ErrorInfo(status, requestURL, exceptionMessage)));
        } catch (JsonProcessingException e1) {
            logger.error("Could not process to JSON the given ErrorInfo instance", e1);
            return ResponseEntity.status(status)
                    .headers(responseHeaders).body("");
        }
    }
}
