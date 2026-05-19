package org.reactome.server.orcid.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class OrcidAuthorisationException extends WorkClaimException {
    public OrcidAuthorisationException() {
        super();
    }

    public OrcidAuthorisationException(String msg) {
        super(msg);
    }

}
