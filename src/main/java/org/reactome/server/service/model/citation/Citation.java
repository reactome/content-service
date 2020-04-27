package org.reactome.server.service.model.citation;

import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Map;

public abstract class Citation {
    private String id;
    private String title;
    private List<Map<String, String>> authors;
    private String year;
    private String month;
    private String doi;
    private List<String> urls;


    public Citation(String id, String title) {
        if (title == null || id == null) {
            throw new IllegalArgumentException("Title or id can not be null");
        }
        this.id = id;
        this.title = title;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Map<String, String>> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Map<String, String>> authors) {
        this.authors = authors;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        Integer integerMonth = Integer.parseInt(month);
        if (integerMonth > 0 && integerMonth <= 12) {
            this.month = new DateFormatSymbols().getShortMonths()[integerMonth-1];
        }
    }

    // converts a citation to RIS string
    // RIS file specs were taken from:
    // https://web.archive.org/web/20100704171416/http://www.refman.com/support/risformat_intro.asp
    // https://en.wikipedia.org/wiki/RIS_(file_format)
    public abstract String toRIS(String dateOfAccess);

    // converts citation into a BibTeX string
    // BibTeX files specs were taken from:
    // LaTeX - User's Guide and Reference Manual-lamport94.pdf, Appendix B
    // https://www.economics.utoronto.ca/osborne/latex/BIBTEX.HTM
    // https://en.wikipedia.org/wiki/BibTeX
    public abstract String toBibtex(String dateOfAccess);

    public abstract String toText(String dateOfAccess);
}
