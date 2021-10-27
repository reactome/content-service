package org.reactome.server.service.controller.interactors;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;


public class TokenControllerTest extends BaseTest {

    @Value("${tuples.uploaded.files.folder:/Users/reactome/Reactome/custom}")
    private String tokenFolder;

    @Test
    public void getInteractors() throws Exception {
        String token = getTokenFromLastestPSIFile(tokenFolder);

        /*
          To test it, you will need a valid token, which can be generated when running the CustomPsicquicControllerTest.
          The token (PSI--XXXXX.bin) will be written in the file system based on the <tuples.uploaded.files.folder> property.
          e.g <tuples.uploaded.files.folder>/Users/reactome/Reactome/custom</tuples.uploaded.files.folder>
         */
        //Todo this token only works for local testing
        mockMvcPostResult("/interactors/token/" + token, "Q9UBU9");
    }

    /* Get the newest file for a specific extension */
    private String getTokenFromLastestPSIFile(String filePath) {
        String token;
        File dir = new File(filePath);
        FileFilter fileFilter = new WildcardFileFilter("PSI*");
        File[] files = dir.listFiles(fileFilter);
        if (files != null && files.length > 0) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            token = StringUtils.substringBefore(files[0].getName(), ".");
            return token;
        }
        return "-";
    }
}