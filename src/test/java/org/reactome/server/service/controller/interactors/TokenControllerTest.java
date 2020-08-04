package org.reactome.server.service.controller.interactors;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class TokenControllerTest extends BaseTest {

    @Value("${tuples.uploaded.files.folder:/Users/reactome/Reactome/custom}")
    private String tokenFolder;

    @Test
    public void getInteractors() throws Exception {
        String token = getLastestPSIFile(tokenFolder);

        /*
          To test it, you will need a valid token, which can be generated when running the CustomPsicquicControllerTest.
          The token (PSI--XXXXX.bin) will be written in the file system based on the <tuples.uploaded.files.folder> property.
          e.g <tuples.uploaded.files.folder>/Users/reactome/Reactome/custom</tuples.uploaded.files.folder>
         */
        //Todo this token only works for local testing
        mockMvcPostResult("/interactors/token/" + token, "Q9UBU9");
    }

    /* Get the newest file for a specific extension */
    private String getLastestPSIFile(String filePath) {
        File dir = new File(filePath);
        FileFilter fileFilter = new WildcardFileFilter("PSI*");
        File[] files = dir.listFiles(fileFilter);
        if (files != null && files.length > 0) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            return files[0].getName();
        }
        return "-";
    }
}