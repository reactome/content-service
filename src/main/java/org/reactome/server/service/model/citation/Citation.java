package org.reactome.server.service.model.citation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Citation {

    private String id;
    private String title;
    private String journal;
    private String year;
    private String month;
    private String number;
    private String volume;
    private String issn;
    private String pages;
    private String doi;
    private List<String> urls;
    private boolean isPathway;
    private List<Map<String, String>> authors;
    private static final String DEFAULT_AUTHOR_STRING = "The Reactome Consortium";

    public void setId(String id) {
        this.id = id;
    }

    public static String getDefaultAuthorString() {
        return DEFAULT_AUTHOR_STRING;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public void setAuthors(List<Map<String, String>> authors) {
        this.authors = authors;
    }

    public void setPathway(boolean pathway) {
        isPathway = pathway;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getJournal() {
        return journal;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getNumber() {
        return number;
    }

    public String getVolume() {
        return volume;
    }

    public String getIssn() {
        return issn;
    }

    public String getPages() {
        return pages;
    }

    public String getDoi() {
        return doi;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<Map<String, String>> getAuthors() {
        return authors;
    }

    public boolean isPathway() {
        return isPathway;
    }

    // converts citation into a BibTeX string
    // BibTeX files specs were taken from:
    // LaTeX - User's Guide and Reference Manual-lamport94.pdf, Appendix B
    // https://www.economics.utoronto.ca/osborne/latex/BIBTEX.HTM
    // https://en.wikipedia.org/wiki/BibTeX
    public String toBibTeX() {
        String newline = "\n";
        String openBracket = "{";
        String closeBracket = "}";
        String comma = ",";
        String endLine = closeBracket + comma + newline;
        String space = " ";
        String and = "and";

    // start bibtex document
    // https://tex.stackexchange.com/questions/3587/how-can-i-use-bibtex-to-cite-a-web-page
    String bibtex = "@" + (this.isPathway ? "misc" : "article") + openBracket + this.id + comma + newline;

    bibtex += "Title = " + openBracket + this.getTitle() + endLine; // added title

    // prepping author string
    String authorString = "";
    if(this.authors != null && !this.authors.isEmpty()) {
        List<String> authorDetails = new ArrayList<>();
        for (Map<String, String> author: authors) {
            authorDetails.add(author.get("lastName") + comma + space + (author.containsKey("firstName") ? author.get("firstName"):author.get("initials")));
        }
        authorString = String.join(space + and + space, authorDetails);
    }
    else {
        authorString = DEFAULT_AUTHOR_STRING;
    }

    bibtex += "Author = " + openBracket + authorString + endLine;

    // all the journal related info
    if (this.journal != null) bibtex += "Journal = " + openBracket + this.journal + endLine;
    if (this.number != null) bibtex += "Number = " + openBracket + this.number + endLine;
    if (this.volume != null) bibtex += "Volume = " + openBracket + this.volume + endLine;
    if (this.issn != null) bibtex += "ISSN = " + openBracket + this.issn + endLine;
    if (this.pages != null) bibtex += "Pages = " + openBracket + this.pages + endLine;

    // all the pub time related info
    bibtex += "Year = " + openBracket + this.year + endLine;
    if (this.month != null) bibtex += "Month = " + openBracket + this.month + endLine;

    // any unique identifiers
    if (this.doi != null) bibtex += "DOI = " + openBracket + this.doi + endLine;
    if (this.urls != null && !this.urls.isEmpty()) bibtex += "URL = " + openBracket + this.urls.get(0) + endLine;

    // end bibtex document
    bibtex += closeBracket;

    return bibtex;
    }

    // converts a citation to RIS string
    // RIS file specs were taken from:
    // https://web.archive.org/web/20100704171416/http://www.refman.com/support/risformat_intro.asp
    // https://en.wikipedia.org/wiki/RIS_(file_format)
    public String toRIS() {
        String doubleSpace = "  ";
        String space = " ";
        String dash = "-";
        String keyValueSeparator = doubleSpace + dash + space;
        String comma = ",";
        String newline = "\n";

        // start the RIS document
        String ris = "TY" + keyValueSeparator + (this.isPathway ? "ELEC" : "JOUR") + newline;

        // adding the title
        ris += "TI" + keyValueSeparator + this.title + newline;

        // adding the authors
        String authorString = "";
        if(this.authors != null && !this.authors.isEmpty()) {
            for (Map<String, String> author: authors) {
                authorString += "AU" + keyValueSeparator + author.get("lastName") + comma + space + (author.containsKey("firstName") ? author.get("firstName"):author.get("initials")) + newline;
            }
        }
        else {
            authorString = "AU" + keyValueSeparator + DEFAULT_AUTHOR_STRING + newline;
        }
    ris += authorString;

    // adding unique identifiers
    // Accession Number
    ris += "AN" + keyValueSeparator + this.id + newline;
    // DOI
    if (this.doi != null) ris += "DO" + keyValueSeparator + this.doi + newline;
    // URL
    if (this.urls != null && !this.urls.isEmpty()) {
        String urlstring = "";
        for(String url : urls){
            urlstring += "UR" + keyValueSeparator + url.trim() + newline;
        }
        ris += urlstring;
    }

    //all the pub time related info
    // publishing year
    ris += "PY" + keyValueSeparator + this.year + newline;
    if (this.month != null) ris += "DA" + keyValueSeparator + this.year + "/" + this.month + "//" + newline;

    // all the journal related fields
    if (this.journal != null) ris += "JO" + keyValueSeparator + this.journal + newline;
    if (this.number != null) ris += "IS" + keyValueSeparator + this.number + newline;
    if (this.volume != null) ris += "VL" + keyValueSeparator + this.volume + newline;
    if (this.issn != null) ris += "SN" + keyValueSeparator + this.issn + newline;
    if (this.pages != null) ris += "SP" + keyValueSeparator + this.pages + newline;

    // end of RIS document
    ris += "ER" + keyValueSeparator + newline;

    return ris;
    }

}
