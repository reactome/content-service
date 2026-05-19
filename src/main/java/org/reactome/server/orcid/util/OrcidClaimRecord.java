package org.reactome.server.orcid.util;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class OrcidClaimRecord {

    private String stId;
    private String orcid;
    private Long putCode;
    private String created;
    private String lastModified;

    public OrcidClaimRecord() {
    }

    public OrcidClaimRecord(String stId, String orcid, Long putCode, String created, String lastModified) {
        this.stId = stId;
        this.orcid = orcid;
        this.putCode = putCode;
        this.created = created;
        this.lastModified = lastModified;
    }

    public String getStId() {
        return stId;
    }

    public void setStId(String stId) {
        this.stId = stId;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public Long getPutCode() {
        return putCode;
    }

    public void setPutCode(Long putCode) {
        this.putCode = putCode;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
