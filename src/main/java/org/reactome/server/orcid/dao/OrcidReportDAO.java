package org.reactome.server.orcid.dao;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.reactome.server.orcid.domain.*;
import org.reactome.server.orcid.util.OrcidClaimRecord;
import org.reactome.server.orcid.util.OrcidHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@Component
public class OrcidReportDAO {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private static final String REPORT_ENDPOINT = "/report/orcid/";

    // URLs
    private static final String REPORT_ORCIDCLAIMREGISTER = "claim/register";
    private static final String REPORT_ORCIDCLAIMLOAD = "claim/load/%s";
    private static final String REPORT_ORCIDTOKENREGISTER = "token/register";
    private static final String REPORT_ORCIDTOKENLOAD = "token/load/%s";

    private static List<OrcidClaimRecord> pendingList = new ArrayList<>();

    @Value("${report.url:http://localhost:8080}")
    private String reportUrl;
    @Value("${report.user:default}")
    private String reportUser;
    @Value("${report.password:default}")
    private String reportPassword;

    private OrcidHelper orcidHelper;

    public void asyncPersistResponse(OrcidToken tokenSession, WorkBulkResponse workBulkResponse) {
        String orcid = tokenSession.getOrcid();
        List<OrcidClaimRecord> list = new ArrayList<>();
        for (WorkResponse ss : workBulkResponse.getBulk()) {
            if (ss.getWork() != null) {
                String stId = "";
                for (ExternalId eid : ss.getWork().getExternalIds().getExternalId()) {
                    if (eid.getType().equalsIgnoreCase(ExternalIdType.OTHERID.getName())) stId = eid.getValue();
                }
                String createdDate = (ss.getWork().getCreatedDate() != null) ? ss.getWork().getCreatedDate().getContent() : "";
                String lastModifiedDate = (ss.getWork().getLastModifiedDate() != null) ? ss.getWork().getLastModifiedDate().getContent() : "";
                list.add(new OrcidClaimRecord(stId, orcid, ss.getWork().getPutCode(), createdDate, lastModifiedDate));
            }
        }
        new Thread(() -> persistClaimingResponse(list), "ReportOrcidThread").start();
    }

    public void asyncPersistOrcidToken(OrcidToken orcidToken) {
        new Thread(() -> persistToken(orcidToken), "ReportOrcidTokenThread").start();
    }

    /**
     * putcodes are added to pendingList if they can't be persisted in the report project, than ynchronise pending list every hour
     */
    @Scheduled(cron = "0 0 */1 * * *")
    private synchronized void persistPendingList() {
        if (pendingList != null && pendingList.size() > 0) persistClaimingResponse(pendingList);
    }

    private synchronized void persistClaimingResponse(List<OrcidClaimRecord> list) {
        try {
            int statusCode = httpPost(REPORT_ORCIDCLAIMREGISTER, list);
            if (statusCode != 200) {
                pendingList.clear();
                pendingList.addAll(list);
                infoLogger.warn("[REPORT_ORCID011] Putcodes haven't been saved. StatusCode: " + statusCode);
            } else {
                pendingList.clear();
            }
        } catch (Throwable e) { // don't create pendingList if report is unavailable.
            infoLogger.warn("[REPORT_ORCID001] Report project is unavailable.", e.getMessage());
        }
    }

    public List<OrcidClaimRecord> load(String orcid) {
        try {
            CloseableHttpClient client = getHttpClient();
            HttpGet httpGet = new HttpGet(REPORT_ENDPOINT + String.format(REPORT_ORCIDCLAIMLOAD, orcid));
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return orcidHelper.marshaller(json);
            }
        } catch (IOException e) {
            // something here maybe
        }
        return null;
    }

    private synchronized void persistToken(OrcidToken orcidToken) {
        int statusCode = httpPost(REPORT_ORCIDTOKENREGISTER, orcidToken);
        if (statusCode != 200) infoLogger.warn("[REPORT_ORCID022] Couldn't store Token. StatusCode: " + statusCode);
    }

    private CloseableHttpClient getHttpClient() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(this.reportUser, this.reportPassword);
        provider.setCredentials(AuthScope.ANY, credentials);
        return HttpClients.custom().setDefaultCredentialsProvider(provider).build();
    }

    private int httpPost(String pathMapping, Object entity) {
        int ret = -1;
        try {
            CloseableHttpClient client = getHttpClient();
            HttpPost httpPost = new HttpPost(reportUrl + REPORT_ENDPOINT + pathMapping);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(orcidHelper.unmarshaller(entity)));
            CloseableHttpResponse response = client.execute(httpPost);
            ret = response.getStatusLine().getStatusCode();
        } catch (Throwable e) { // don't create pendingList if report is unavailable.
            infoLogger.warn("[REPORT_ORCID001] Report project is unavailable.", e.getMessage());
        }
        return ret;
    }

    @Autowired
    public void setOrcidHelper(OrcidHelper orcidHelper) {
        this.orcidHelper = orcidHelper;
    }
}
