package org.reactome.server.service.exception;

import org.reactome.server.search.domain.TargetResult;

import java.util.Set;

/**
 * THe SolR search may return no results for in Reactome, but the term might be found in our scope of annotation.
 * This exception tells the GlobalExceptionHandler to return 404 and also add the targets to the response body
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class NoResultsFoundException extends NotFoundException {
    private Set<TargetResult> targets;

    public NoResultsFoundException(String message, Set<TargetResult> targets) {
        super(message);
        this.targets = targets;
    }

    public Set<TargetResult> getTargets() {
        return targets;
    }
}
