package org.reactome.server.tools.utils;

import org.springframework.context.annotation.Scope;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Scope("singleton")
public class TuplesFilesManager {

    private String pathDirectory;

    public void setPathDirectory(String pathDirectory) {
        this.pathDirectory = pathDirectory;
    }

}
