package org.reactome.server.orcid.exception;

import org.reactome.server.orcid.domain.OrcidToken;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class OrcidOAuthException extends Exception {
    private OrcidToken orcidToken;

    public OrcidOAuthException(String msg) {
        super(msg);
    }

    public OrcidOAuthException(String msg, OrcidToken orcidToken) {
        super(msg);
        this.orcidToken = orcidToken;
    }

    public OrcidToken getOrcidToken() {
        return orcidToken;
    }
}
