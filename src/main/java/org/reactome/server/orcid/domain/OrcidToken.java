package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class OrcidToken implements Serializable, Comparable<OrcidToken> {
    //{"access_token":"56191de4-9dd9-4afa-8d6b-0f03816001e3","token_type":"bearer","refresh_token":"b8c1f9a0-ffaa-464a-9728-6076c356a1ca","expires_in":631138518,"scope":"/authenticate","name":"Guilherme Viteri","orcid":"0000-0002-5910-2066"}

    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private Long expiresIn;
    private String scope;
    private String name;
    private String orcid;
    private String error;
    private String errorDescription;

    public OrcidToken() {
    }

    public OrcidToken(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonProperty("token_type")
    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @JsonProperty("refresh_token")
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @JsonProperty("expires_in")
    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonProperty("error_description")
    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @Override
    public String toString() {
        return "OrcidToken{" +
                "accessToken='********'" +
                ", tokenType='" + tokenType + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn='" + expiresIn + '\'' +
                ", scope='" + scope + '\'' +
                ", name='" + name + '\'' +
                ", orcid='" + orcid + '\'' +
                ", error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrcidToken that = (OrcidToken) o;
        return Objects.equals(orcid, that.orcid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orcid);
    }

    @Override
    public int compareTo(@NonNull OrcidToken o) {
        return this.orcid.compareTo(o.getOrcid());
    }
}
