package org.reactome.server.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.neo4j.ogm.exception.ConnectionException;
import org.reactome.server.interactors.exception.CustomPsicquicInteractionClusterException;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.exception.PsicquicResourceNotFoundException;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.exception.TupleParserException;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.*;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonDeserializationException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramJsonNotFoundException;
import org.reactome.server.tools.diagram.exporter.common.profiles.factory.DiagramProfileException;
import org.reactome.server.tools.diagram.exporter.pptx.exception.LicenseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    //================================================================================
    // NotFound
    //================================================================================

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    ErrorInfo handleNotFoundException(HttpServletRequest request, NotFoundException e) {
        //no logging here!
        return new ErrorInfo(HttpStatus.NOT_FOUND, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundTextPlainException.class)
    @ResponseBody
    ResponseEntity<String> handleNotFoundTextPlainException(HttpServletRequest request, NotFoundTextPlainException e) {
        //no logging here!
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e);
    }

    //================================================================================
    // SOLR
    //================================================================================

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SolrSearcherException.class)
    @ResponseBody
    ErrorInfo handleSolrException(HttpServletRequest request, SolrSearcherException e) {
        logger.error("Solr exception was caught for request: " + request.getRequestURL(), e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    //================================================================================
    // Interactors
    //================================================================================

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StaticInteractionException.class)
    @ResponseBody
    ErrorInfo handleStaticInteractionException(HttpServletRequest request, StaticInteractionException e) {
        logger.warn("StaticInteractionException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicContentException.class)
    @ResponseBody
    ErrorInfo handlePsicquicContentException(HttpServletRequest request, PsicquicContentException e) {
        logger.warn("PsicquicContentException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicQueryException.class)
    @ResponseBody
    ErrorInfo handlePsicquicQueryException(HttpServletRequest request, PsicquicQueryException e) {
        logger.warn("PsicquicQueryException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "PSICQUIC resource is not responding");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicResourceNotFoundException.class)
    @ResponseBody
    ErrorInfo handlePsicquicResourceNotFoundException(HttpServletRequest request, PsicquicResourceNotFoundException e) {
        logger.warn("PsicquicResourceNotFoundException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsimiTabException.class)
    @ResponseBody
    ErrorInfo handlePsimiTabException(HttpServletRequest request, PsimiTabException e) {
        logger.warn("PsimiTabException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "Couldn't parse PSICQUIC result");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicRegistryClientException.class)
    @ResponseBody
    ErrorInfo handlePsicquicRegistryClientException(HttpServletRequest request, PsicquicRegistryClientException e) {
        logger.warn("PsicquicRegistryClientException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "Couldn't query PSICQUIC Resources");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomPsicquicInteractionClusterException.class)
    @ResponseBody
    ErrorInfo handleCustomPsicquicInteractionClusterException(HttpServletRequest request, CustomPsicquicInteractionClusterException e) {
        logger.warn("CustomPsicquicInteractionClusterException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "Error querying your PSICQUIC Resource.");
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
    ErrorInfo handleParserException(HttpServletRequest request, ParserException e) {
        logger.warn("ParserException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InteractorResourceNotFound.class)
    @ResponseBody
    ErrorInfo handleInteractorResourceNotFound(HttpServletRequest request, InteractorResourceNotFound e) {
        logger.warn("InteractorResourceNotFound was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.NOT_FOUND, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
    @ExceptionHandler(RequestEntityTooLargeException.class)
    @ResponseBody
    ErrorInfo handleRequestEntityTooLargeException(HttpServletRequest request, RequestEntityTooLargeException e) {
        logger.warn("RequestEntityTooLargeException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseBody
    ErrorInfo handleUnprocessableEntityException(HttpServletRequest request, UnprocessableEntityException e) {
        logger.warn("UnprocessableEntityException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TokenNotFoundException.class)
    @ResponseBody
    ErrorInfo handleTokenNotFoundException(HttpServletRequest request, TokenNotFoundException e) {
        logger.warn("TokenNotFoundException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.NOT_FOUND, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(UnsupportedMediaTypeException.class)
    @ResponseBody
    ErrorInfo handleUnsupportedMediaTypeException(HttpServletRequest request, UnsupportedMediaTypeException e) {
        logger.warn("UnsupportedMediaTypeException was caught for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    ErrorInfo handleMaxUploadSizeExceededException(HttpServletRequest request, MaxUploadSizeExceededException e) {
        logger.warn("UnsupportedMediaTypeException was caught for request: " + request.getRequestURL());
        String msg = "Maximum upload size of " + e.getMaxUploadSize() + " bytes exceeded";
        return new ErrorInfo(HttpStatus.PAYLOAD_TOO_LARGE, request.getRequestURL(), msg);
    }

    //================================================================================
    // Neo4j
    //================================================================================
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ConnectionException.class)
    @ResponseBody
    ErrorInfo handleNeo4jConnectionException(HttpServletRequest request, ConnectionException e) {
        logger.error("Neo4j ConnectionException was caught for request: " + request.getRequestURL(), e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    //================================================================================
    // Diagram Exporter
    //================================================================================

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DiagramJsonNotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleDiagramJsonNotFoundException(HttpServletRequest request, DiagramJsonNotFoundException e) {
        logger.warn("DiagramJsonNotFoundException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DiagramJsonDeserializationException.class)
    @ResponseBody
    ResponseEntity<String> handleDiagramJsonDeserializationException(HttpServletRequest request, DiagramJsonDeserializationException e) {
        logger.warn("DiagramJsonDeserializationException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DiagramProfileException.class)
    @ResponseBody
    ResponseEntity<String> handleDiagramProfileException(HttpServletRequest request, DiagramProfileException e) {
        logger.warn("DiagramProfileException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(LicenseException.class)
    @ResponseBody
    ResponseEntity handleLicenseException(HttpServletRequest request, LicenseException e) {
        // Aspose License is expired. The file is not generated and 503 is thrown
        logger.warn("LicenseException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.SERVICE_UNAVAILABLE, request, e);
    }

    //================================================================================
    // Default
    //================================================================================

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CertificateException.class)
    @ResponseBody
    ErrorInfo handleCertificateException(HttpServletRequest request, CertificateException e) {
        logger.error("CertificateException was caught for request: " + request.getRequestURL(), e);
        return new ErrorInfo(HttpStatus.BAD_REQUEST, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({InvocationTargetException.class, IllegalAccessException.class})
    @ResponseBody
    ErrorInfo handleReflectionError(HttpServletRequest request, Exception e) {
        logger.error("ReflectionException was caught for request: " + request.getRequestURL(), e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ClassNotFoundException.class)
    @ResponseBody
    ErrorInfo handleClassNotFoundException(HttpServletRequest request, ClassNotFoundException e) {
        logger.error("ClassNotFoundException was caught for request: " + request.getRequestURL(), e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "Class specified was not found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    ErrorInfo handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        logger.warn("HttpRequestMethodNotSupportedException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    ErrorInfo handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
        logger.warn("HttpMediaTypeNotSupportedException: " + request.getRequestURL(), e.getMessage());
        return new ErrorInfo(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    ErrorInfo handleHttpMediaTypeNotAcceptableException(HttpServletRequest request, HttpMediaTypeNotAcceptableException e) {
        logger.warn("HttpMediaTypeNotSupportedException: " + request.getRequestURL(), e.getMessage());
        return new ErrorInfo(HttpStatus.NOT_ACCEPTABLE, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    ErrorInfo handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
        logger.warn("MissingServletRequestParameterException: " + request.getRequestURL(), e.getMessage());
        return new ErrorInfo(HttpStatus.BAD_REQUEST, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ErrorInfo handleUnclassified(HttpServletRequest request, Exception e) {
        logger.error("An unspecified exception was caught for request: " + request.getRequestURL(), e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "An unspecified exception was caught");
    }

    /*
     * Adding a JSON String manually to the response.
     *
     * Some services return a binary file or text/plain, etc. Then an ErrorInfo instance is manually converted
     * to JSON and written down in the response body.
     */
    private ResponseEntity<String> toJsonResponse(HttpStatus status, HttpServletRequest request, Exception e) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            return ResponseEntity.status(status)
                    .headers(responseHeaders)
                    .body(mapper.writeValueAsString(new ErrorInfo(status, request.getRequestURL(), e.getMessage())));
        } catch (JsonProcessingException e1) {
            logger.error("Could not process to JSON the given ErrorInfo instance", e1);
            return ResponseEntity.status(status)
                    .headers(responseHeaders).body("");
        }
    }
}
