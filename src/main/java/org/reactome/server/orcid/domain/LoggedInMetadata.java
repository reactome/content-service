package org.reactome.server.orcid.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class LoggedInMetadata {
    private List<AuthorMetadada> author = new ArrayList<>();

    public LoggedInMetadata() {
    }

    public LoggedInMetadata(OrcidToken orcidToken) {
        author.add(new AuthorMetadada(orcidToken.getOrcid(), orcidToken.getName()));
    }

    public List<AuthorMetadada> getAuthor() {
        return author;
    }
}
