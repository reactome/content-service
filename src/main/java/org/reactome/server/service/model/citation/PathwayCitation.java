package org.reactome.server.service.model.citation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PathwayCitation extends Citation {
    private String reactomeReleaseVersion;
    private static final String DEFAULT_AUTHOR_STRING = "The Reactome Consortium";

    public PathwayCitation(String id, String title) {
        super(id, title);
    }

    public String getReactomeReleaseVersion() {
        return reactomeReleaseVersion;
    }

    public void setReactomeReleaseVersion(String reactomeReleaseVersion) {
        this.reactomeReleaseVersion = reactomeReleaseVersion;
    }

    // converts pathway citation to RIS
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
        ris += "TY" + keyValueSeparator + "ELEC" + newline;
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

        // end of RIS document
        ris += "ER" + keyValueSeparator + newline;

        return ris;
    }

    // converts pathway citation to bibtex
    @Override
    public String toBibtex(String dateOfAccess) {

        // Pathway Citations are to be considered as `misc`
        // there are no compulsory fields for this type

        String newline = "\n";
        String openBracket = "{";
        String closeBracket = "}";
        String comma = ",";
        String endLine = closeBracket + comma + newline;
        String space = " ";
        String and = "and";

        // start bibtex document
        // https://tex.stackexchange.com/questions/3587/how-can-i-use-bibtex-to-cite-a-web-page
        String bibtex = "@" + "misc" + openBracket + this.getId() + comma + newline;

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
            authorString = DEFAULT_AUTHOR_STRING;
        }
        bibtex += "Author = " + openBracket + authorString + endLine;

        // all the pub time related info
        if (this.getYear() != null) bibtex += "Year = " + openBracket + this.getYear() + endLine;
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

    // converts citation to textual format
    public String toText(String dateOfAccess) {
        return "Pathway Citation: " + pathwayCitation(dateOfAccess) + "\n" + "Image Citation: " + imageCitation(dateOfAccess);
    }

    public String pathwayCitation(String dateOfAccess) {
        String pathwayCitation;

        // adding authors
        if (this.getAuthors() != null && !this.getAuthors().isEmpty()) {
            List<String> authorDetails = new ArrayList<>();
            for (Map<String, String> author : this.getAuthors()) {
                authorDetails.add(author.get("lastName") + "," + " " + author.get("initials"));
            }
            String authorCitation = authorDetails.get(authorDetails.size() - 1);
            if (authorDetails.size() > 1) {
                authorCitation = String.join(" " + ",", authorDetails.subList(0,authorDetails.size() - 1)) + " & " + authorCitation;
            }

            pathwayCitation = authorCitation;
        } else {
            pathwayCitation = DEFAULT_AUTHOR_STRING;
        }

        if (this.getYear() != null) pathwayCitation += " (" + this.getYear() + "). "; // setting year
        pathwayCitation += commonCitation(dateOfAccess);
        return pathwayCitation;
    }

    public String imageCitation(String dateOfAccess) {
        return "Image Citation for " + commonCitation(dateOfAccess);
    }

    private String commonCitation(String dateOfAccess) {
        String citation = "";

        citation += this.getTitle() + ". ";
        if (this.reactomeReleaseVersion != null) citation += "Reactome" + ", " + reactomeReleaseVersion + ", ";
        if (this.getUrls() != null && !this.getUrls().isEmpty()) {
            citation += String.join(", ", this.getUrls()) + " ";
        }
        citation += "(" + dateOfAccess + ")";

        return citation;
    }


}
