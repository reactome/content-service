package org.reactome.server.tools.handler;

import org.reactome.server.tools.exception.ContentServiceError;
import org.reactome.server.tools.exception.ContentServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * By implementing the HandlerExceptionResolver we can check for different exceptions happened
 * during handler mapping or execution. Different actions can be developed depending of the
 * exception type.
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class HandlerExceptionResolverImpl implements HandlerExceptionResolver {

    final static Logger logger = LoggerFactory.getLogger(HandlerExceptionResolverImpl.class);

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {

        response.setContentType("application/json");

        if (ex instanceof ContentServiceException) {
            ContentServiceException cse = (ContentServiceException) ex;

            response.setStatus(cse.getHttpStatus().value());
            ContentServiceError error = new ContentServiceError(cse);
            try {
                response.getWriter().println(error);
            } catch (IOException e) {
                logger.error("Error writing to output stream", e);
            }
            return new ModelAndView();
        }

        ex.printStackTrace();

        return null;
    }
}