package org.reactome.server.service.model.citation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaticCitation extends Citation {

    private String journal;
    private String number;
    private String volume;
    private String issn;
    private String pages;
    private String pmid;
    private String pmcid;

    public StaticCitation(String id, String title) {
        super(id, title);
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getPmcid() {
        return pmcid;
    }

    public void setPmcid(String pmcid) {
        this.pmcid = pmcid;
    }


    // converts static citation to BiBTex
    @Override
    public String toBibtex(String dateOfAccess) {

        // Static citations are to be considered as `articles`
        // Required fields: author, title, journal, year, volume
        // Optional fields: number, pages, month, doi, note, key

        String newline = "\n";
        String openBracket = "{";
        String closeBracket = "}";
        String comma = ",";
        String endLine = closeBracket + comma + newline;
        String space = " ";
        String and = "and";

        // start bibtex document
        String bibtex = "@" + ("article") + openBracket + this.getId() + comma + newline;
        bibtex += "Title = " + openBracket + this.getTitle() + endLine; // added title

        // prepping author string
        String authorString;
        if (this.getAuthors() != null && !this.getAuthors().isEmpty()) {
            List<String> authorDetails = new ArrayList<>();
            for (Map<String, String> author : this.getAuthors()) {
                authorDetails.add(author.get("lastName") + comma + space + (author.containsKey("firstName") ? author.get("firstName") : author.get("initials")));
            }
            authorString = String.join(space + and + space, authorDetails);
        } else {
            authorString = "No authors specified";
        }
        bibtex += "Author = " + openBracket + authorString + endLine; //compulsory field

        // all the journal related info
        bibtex += "Journal = " + openBracket + (this.getJournal() != null ? this.getJournal() : "No journal specified") + endLine; // compulsory field
        if (this.getNumber() != null) bibtex += "Number = " + openBracket + this.getNumber() + endLine;
        bibtex += "Volume = " + openBracket + (this.getVolume() != null ? this.getVolume() : "No volume specified") + endLine; // compulsory field
        if (this.getIssn() != null) bibtex += "ISSN = " + openBracket + this.getIssn() + endLine;
        if (this.getPages() != null) bibtex += "Pages = " + openBracket + this.getPages() + endLine;

        // all the pub time related info
        bibtex += "Year = " + openBracket + (this.getYear() != null ? this.getVolume() : "No year specified") + endLine; // compulsory field
        if (this.getMonth() != null) bibtex += "Month = " + openBracket + this.getMonth() + endLine;

        // any unique identifiers
        if (this.getDoi() != null) bibtex += "DOI = " + openBracket + this.getDoi() + endLine;
        if (this.getUrls() != null && !this.getUrls().isEmpty())
            bibtex += "URL = " + openBracket + this.getUrls().get(0) + endLine;

        bibtex += "Note = " + openBracket + "Provided by Reactome. Citation Accessed on " + dateOfAccess + endLine;

        // end bibtex document
        bibtex += closeBracket;

        return bibtex;
    }


    //converts static citation to RIS
    @Override
    public String toRIS(String dateOfAccess) {
        // TY and ER are the only tags that are mandatory and have fixed positions.

        String doubleSpace = "  ";
        String space = " ";
        String dash = "-";
        String keyValueSeparator = doubleSpace + dash + space;
        String comma = ",";
        String newline = "\n";

        String ris = "Provided by Reactome. Citation Accessed on " + dateOfAccess + "\n\n";


        // start the RIS document
        ris += "TY" + keyValueSeparator + "JOUR" + newline;
        // adding the title
        ris += "TI" + keyValueSeparator + this.getTitle() + newline;

        // adding the authors
        String authorString = "";
        if (this.getAuthors() != null && !this.getAuthors().isEmpty()) {
            for (Map<String, String> author : this.getAuthors()) {
                authorString += "AU" + keyValueSeparator + author.get("lastName") + comma + space + (author.containsKey("firstName") ? author.get("firstName") : author.get("initials")) + newline;
            }
            ris += authorString;
        }

        // adding unique identifiers
        // Accession Number
        ris += "AN" + keyValueSeparator + this.getId() + newline;
        // DOI
        if (this.getDoi() != null) ris += "DO" + keyValueSeparator + this.getDoi() + newline;
        // URL
        if (this.getUrls() != null && !this.getUrls().isEmpty()) {
            String urlstring = "";
            for (String url : this.getUrls()) {
                urlstring += "UR" + keyValueSeparator + url.trim() + newline;
            }
            ris += urlstring;
        }

        // all the pub time related info
        // publishing year
        if (this.getYear() != null) {
            ris += "PY" + keyValueSeparator + this.getYear() + newline;
            if (this.getMonth() != null) {
                ris += "DA" + keyValueSeparator + this.getYear() + "/" + this.getMonth() + "//" + newline;
            }
        }

        // all the journal related fields
        if (this.getJournal() != null) ris += "JO" + keyValueSeparator + this.getJournal() + newline;
        if (this.getNumber() != null) ris += "IS" + keyValueSeparator + this.getNumber() + newline;
        if (this.getVolume() != null) ris += "VL" + keyValueSeparator + this.getVolume() + newline;
        if (this.getIssn() != null) ris += "SN" + keyValueSeparator + this.getIssn() + newline;
        if (this.getPages() != null) ris += "SP" + keyValueSeparator + this.getPages() + newline;

        // end of RIS document
        ris += "ER" + keyValueSeparator + newline;

        return ris;
    }

    // converts citation to textual format
    @Override
    public String toText(String dateOfAccess) {
        String text = "Provided by Reactome. Citation Accessed on " + dateOfAccess + "\n\n";
        String space = " ";
        String period = ".";

        // setting authors
        if (this.getAuthors() != null && !this.getAuthors().isEmpty()) {
            List<String> authorDetails = new ArrayList<>();
            for (Map<String, String> author : this.getAuthors()) {
                authorDetails.add(author.get("fullName"));
            }
            text += String.join("," + space, authorDetails) + period + space;
        }

        // setting title of publication
        text += this.getTitle() + period + space;

        // setting Journal name
        if (this.getJournal() != null) text += this.getJournal() + period + space;
        // setting publication date
        if (this.getYear() != null) {
            text += this.getYear();

            if (this.getMonth() != null) {
                text += "/" + this.getMonth();
            }
            text += period + space;
        }

        // setting volume and issue number
        if (this.getVolume() != null) {
            text += this.getVolume();
            if (this.getNumber() != null) {
                text += "(" + this.getNumber() + ")";
            }
            text += period + space;
        }
        // setting page info
        if (this.getPages() != null) {
            text += this.getPages() + period + space;
        }

        // setting DOI
        if (this.getDoi() != null) {
            text += this.getDoi() + period + space;
        }

        // setting PMID
        if (this.getPmid() != null) {
            text += this.getPmid() + period + space;
        }

        // setting PMCID
        if (this.getPmcid() != null) {
            text += this.getPmcid() + period + space;
        }

        return text;
    }
}
