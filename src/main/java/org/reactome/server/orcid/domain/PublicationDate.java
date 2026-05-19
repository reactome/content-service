package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class PublicationDate {

    @JsonProperty("day")
    private Day day;

    @JsonProperty("month")
    private Month month;

    @JsonProperty("year")
    private Year year;

    @JsonProperty("media-type")
    private String mediaType;

    public PublicationDate() {
    }

    public PublicationDate(Year year, Month month, Day day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public PublicationDate (String dateTime) {
        if (StringUtils.isNotEmpty(dateTime)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDate localdateTime = LocalDate.parse(dateTime, formatter);
            this.day = new Day(localdateTime.getDayOfMonth());
            this.month = new Month(localdateTime.getMonthValue());
            this.year = new Year(localdateTime.getYear());
        }
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    private class Day {
        @JsonProperty("value")
        @JsonRawValue
        private Value content;
        public Day () {}
        Day(int day) {
            this.content = new Value(Integer.toString(day));
        }
        public Value getContent() {
            return content;
        }
        public void setContent(Value content) {
            this.content = content;
        }
    }

    private class Month {
        @JsonProperty("value")
        @JsonRawValue
        private Value content;
        public Month () {}
        Month(int month) { this.content = new Value(Integer.toString(month)); }
        public Value getContent() {
            return content;
        }
        public void setContent(Value content) {
            this.content = content;
        }
    }

    private class Year {
        @JsonProperty("value")
        @JsonRawValue
        private Value content;
        public Year () {}
        Year(int year) {
            this.content = new Value(Integer.toString(year));
        }
        public Value getContent() {
            return content;
        }
        public void setContent(Value content) {
            this.content = content;
        }
    }
}
