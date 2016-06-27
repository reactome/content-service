package org.reactome.server.service.controller;

import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.newExceptions.ErrorInfo;
import org.reactome.server.service.exception.newExceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody ErrorInfo handleNotFoundException(HttpServletRequest request, NotFoundException e) {
        //no logging here!
        return new ErrorInfo(HttpStatus.NOT_FOUND, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({InvocationTargetException.class, IllegalAccessException.class})
    @ResponseBody ErrorInfo handleReflectionError (HttpServletRequest request, Exception e) {
        logger.error("ReflectionException was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SolrSearcherException.class)
    @ResponseBody
    ErrorInfo handleSolrException(HttpServletRequest request, SolrSearcherException e) {
        logger.error("Solr exception was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ClassNotFoundException.class)
    @ResponseBody
    ErrorInfo handleSolrException(HttpServletRequest request, ClassNotFoundException e) {
        logger.error("ClassNotFoundException was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "Class specified was not found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ErrorInfo handleUnclassified(HttpServletRequest request, Exception e) {
        logger.error("An unspecified exception was caught",e);
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURL(), "An unspecified exception was caught");
    }
}
