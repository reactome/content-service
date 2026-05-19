package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class ClaimingSummary implements Serializable {
    private int total;
    private int totalExecuted;
    private int totalIncluded;
    private int totalConflict;
    private int totalErrors;

    @JsonIgnore
    private WorkBulkResponse workBulkResponse;

    public ClaimingSummary() {

    }

    public ClaimingSummary(int total, int totalExecuted, WorkBulkResponse workBulkResponse) {
        this.total = total;
        this.totalExecuted = totalExecuted;
        this.workBulkResponse = workBulkResponse;

        for (WorkResponse ss : workBulkResponse.getBulk()) {
            if (ss.getWork() != null) totalIncluded++;
            if (ss.getError() != null && (ss.getError().getResponseCode().equals("409"))) totalConflict++;
            else if (ss.getError() != null) totalErrors++;
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalExecuted() {
        return totalExecuted;
    }

    public void setTotalExecuted(int totalExecuted) {
        this.totalExecuted = totalExecuted;
    }

    public WorkBulkResponse getWorkBulkResponse() {
        return workBulkResponse;
    }

    public void setWorkBulkResponse(WorkBulkResponse workBulkResponse) {
        this.workBulkResponse = workBulkResponse;
    }

    public int getTotalIncluded() {
        return totalIncluded;
    }

    public int getTotalConflict() {
        return totalConflict;
    }

    public int getTotalErrors() {
        return totalErrors;
    }
}
