package org.reactome.server.orcid.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.reactome.server.service.utils.HostnameUtil.matchesHostname;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Component
public class OrcidServerInterceptor implements HandlerInterceptor {

    @Value("${orcid.server}")
    private String hostname;

    //preHandle: is called before the target handler method is invoked for a given request. If this method returns false, further processing is abandoned i.e. the handler method is not called.
    //postHandle: is called after execution of target handler, but before the view is rendered. Good for post processing of what we started in preHandler method e.g. performance logging.
    //afterCompletion: is called after rendering the view. Good for resource cleanups

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (matchesHostname(hostname)) return true;

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.sendRedirect(request.getServletContext().getContextPath() + "/");
        return false;
    }
}
