package org.reactome.server.service.controller.citation;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.service.utils.BaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class CitationControllerTest extends BaseTest {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM dd yyyy");
    private Calendar calObj = Calendar.getInstance();
    private String date = dateFormatter.format(calObj.getTime());

    @Test
    public void pathwayCitation() throws Exception {
        mockMvcGetResult("/citation/pathway/R-HSA-9612973", "application/json;charset=UTF-8", "dateAccessed", date);
    }

    @Test
    public void downloadCitation() throws Exception {
        mockMvcGetResult("/citation/download", "text/plain;charset=ISO-8859-1");
    }

    @Test
    public void staticCitation() throws Exception {
        mockMvcGetResult("/citation/static/29186351", "text/plain;charset=ISO-8859-1", "dateAccessed", date);
    }

    @Test
    public void export() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("isPathway", false);
        params.put("ext", "bib");
        params.put("id", 29377902);
        params.put("dateAccessed", date);
        mockMvcGetResult("/citation/export", "application/x-bibtex", params);
    }
}