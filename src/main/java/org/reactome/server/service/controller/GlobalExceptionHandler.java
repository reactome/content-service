package org.reactome.server.service.controller;

import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.reactome.server.interactors.exception.CustomPsicquicInteractionClusterException;
import org.reactome.server.interactors.exception.PsicquicQueryException;
import org.reactome.server.interactors.exception.PsicquicResourceNotFoundException;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.exception.TupleParserException;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.controller.graph.NotFoundTextPlainException;
import org.reactome.server.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import psidev.psi.mi.tab.PsimiTabException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;

/**
 * Global exception handler controller
 * This controller will deal with all exceptions thrown by the other controllers if they don't treat them individually
 *
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
        return new ErrorInfo(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundTextPlainException.class)
    @ResponseBody
    String handleNotFoundTextPlainException(HttpServletRequest request, NotFoundTextPlainException e) {
        //no logging here!
        return null;
    }

    //================================================================================
    // SOLR
    //================================================================================

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SolrSearcherException.class)
    @ResponseBody
    ErrorInfo handleSolrException(HttpServletRequest request, SolrSearcherException e) {
        logger.error("Solr exception was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    //================================================================================
    // Interactors
    //================================================================================

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicContentException.class)
    @ResponseBody
    ErrorInfo handlePsicquicContentException(HttpServletRequest request, PsicquicContentException e) {
        logger.warn("PsicquicContentException was caught");
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicQueryException.class)
    @ResponseBody
    ErrorInfo handlePsicquicQueryException(HttpServletRequest request, PsicquicQueryException e) {
        logger.warn("PsicquicQueryException was caught");
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "PSICQUIC resource is not responding");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicResourceNotFoundException.class)
    @ResponseBody
    ErrorInfo handlePsicquicResourceNotFoundException(HttpServletRequest request, PsicquicResourceNotFoundException e) {
        logger.warn("PsicquicResourceNotFoundException was caught");
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsimiTabException.class)
    @ResponseBody
    ErrorInfo handlePsimiTabException(HttpServletRequest request, PsimiTabException e) {
        logger.warn("PsimiTabException was caught");
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't parse PSICQUIC result");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PsicquicRegistryClientException.class)
    @ResponseBody
    ErrorInfo handlePsicquicRegistryClientException(HttpServletRequest request, PsicquicRegistryClientException e) {
        logger.warn("PsicquicRegistryClientException was caught");
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't query PSICQUIC Resources");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomPsicquicInteractionClusterException.class)
    @ResponseBody
    ErrorInfo handleCustomPsicquicInteractionClusterException(HttpServletRequest request, CustomPsicquicInteractionClusterException e) {
        logger.warn("CustomPsicquicInteractionClusterException was caught");
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "Error querying your PSICQUIC Resource.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TupleParserException.class)
    @ResponseBody
    ErrorInfo handleTupleParserException(HttpServletRequest request, TupleParserException e) {
        logger.warn("TupleParserException was caught");
        return new ErrorInfo(HttpStatus.BAD_REQUEST, e.getErrorMessages().toArray(new String[e.getErrorMessages().size()]));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ParserException.class)
    @ResponseBody
    ErrorInfo handleParserException(HttpServletRequest request, ParserException e) {
        logger.warn("ParserException was caught");
        return new ErrorInfo(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InteractorResourceNotFound.class)
    @ResponseBody
    ErrorInfo handleInteractorResourceNotFound(HttpServletRequest request, InteractorResourceNotFound e) {
        logger.warn("InteractorResourceNotFound was caught");
        return new ErrorInfo(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ResponseStatus(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE)
    @ExceptionHandler(RequestEntityTooLargeException.class)
    @ResponseBody
    ErrorInfo handleRequestEntityTooLargeException(HttpServletRequest request, RequestEntityTooLargeException e) {
        logger.warn("RequestEntityTooLargeException was caught");
        return new ErrorInfo(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseBody
    ErrorInfo handleUnprocessableEntityException(HttpServletRequest request, UnprocessableEntityException e) {
        logger.warn("UnprocessableEntityException was caught");
        return new ErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TokenNotFoundException.class)
    @ResponseBody
    ErrorInfo handleTokenNotFoundException(HttpServletRequest request, TokenNotFoundException e) {
        logger.warn("TokenNotFoundException was caught");
        return new ErrorInfo(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(UnsupportedMediaTypeException.class)
    @ResponseBody
    ErrorInfo handleUnsupportedMediaTypeException(HttpServletRequest request, UnsupportedMediaTypeException e) {
        logger.warn("UnsupportedMediaTypeException was caught");
        return new ErrorInfo(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage());
    }

    //================================================================================
    // Default
    //================================================================================

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CertificateException.class)
    @ResponseBody ErrorInfo handleCertificateException (HttpServletRequest request, CertificateException e) {
        logger.error("CertificateException was caught",e);
        return new ErrorInfo(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({InvocationTargetException.class, IllegalAccessException.class})
    @ResponseBody ErrorInfo handleReflectionError (HttpServletRequest request, Exception e) {
        logger.error("ReflectionException was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ClassNotFoundException.class)
    @ResponseBody
    ErrorInfo handleClassNotFoundException(HttpServletRequest request, ClassNotFoundException e) {
        logger.error("ClassNotFoundException was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "Class specified was not found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ErrorInfo handleUnclassified(HttpServletRequest request, Exception e) {
        logger.error("An unspecified exception was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "An unspecified exception was caught");
    }
}
