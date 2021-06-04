package org.reactome.server.service.controller.citation;


import org.junit.jupiter.api.Test;
import org.reactome.server.service.utils.BaseTest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CitationControllerTest extends BaseTest {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM dd yyyy");
    private final Calendar calObj = Calendar.getInstance();
    private final String date = dateFormatter.format(calObj.getTime());

    @Test
    public void pathwayCitation() throws Exception {
        mockMvcGetResult("/citation/pathway/R-HSA-69620", "application/json;charset=UTF-8", "dateAccessed", date);
    }

    @Test
    public void downloadCitation() throws Exception {
        mockMvcGetResult("/citation/download", "text/plain;charset=UTF-8");
    }

    @Test
    public void staticCitation() throws Exception {
        mockMvcGetResult("/citation/static/29186351", "text/plain;charset=UTF-8", "dateAccessed", date);
    }

    @Test
    public void export() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("isPathway", false);
        params.put("ext", "bib");
        params.put("id", 29377902);
        params.put("dateAccessed", date);
        mockMvcGetResult("/citation/export", "application/x-bibtex;charset=UTF-8", params);
    }
}