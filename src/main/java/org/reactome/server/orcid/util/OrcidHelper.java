package org.reactome.server.orcid.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.reactome.server.graph.domain.result.SimpleEventProjection;
import org.reactome.server.orcid.domain.*;
import org.reactome.server.orcid.exception.OrcidAuthorisationException;
import org.reactome.server.orcid.exception.OrcidOAuthException;
import org.reactome.server.orcid.exception.WorkClaimException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@Component
public class OrcidHelper {
    public static final String ORCID_TOKEN = "orcidToken";
    private static final Integer MAX_BULK_POST = 100; // Orcid API won't accept more than 100 per call

    @Value("${orcid.api.baseurl}")
    private String ORCID_API_URI;
    private static final String ORCID_API_VERSION = "v2.1/";
    private static final String ORCID_ALL_WORKS = "##ORCID##/works"; //GET or POST

    // Reactome URLs
    private static final String DETAILS_URL = "https://reactome.org/content/detail/##ID##";
    private static final String PWB_URL = "https://reactome.org/PathwayBrowser/#/##ID##";
    private static final String DOI_URL = "http://dx.doi.org/##ID##";

    public enum ContributionRole {
        AUTHORED, REVIEWED, BOTH
    }

    private Work createWork(SimpleEventProjection event, ContributionRole contributionRole) {
        Work work = new Work();
        work.setWorkTitle(new WorkTitle(event.getDisplayName()));
        boolean isPathway = event.getLabels().contains("Pathway");
        work.setShortDescription((isPathway ? "Pathway" : "Reaction"));
        work.setType("DATA_SET");
        if (event.getDateTime() != null) { // create date can be empty!
            work.setPublicationDate(new PublicationDate(event.getDateTime()));
        }
        work.setUrl(DETAILS_URL.replace("##ID##", event.getStId()));

        work.addExternalId(new ExternalId(ExternalIdType.OTHERID.getName(), event.getStId(), PWB_URL.replace("##ID##", event.getStId()), "SELF"));
        if (isPathway) {
            if (event.getDoi() != null && (StringUtils.isNotEmpty(event.getDoi()) || StringUtils.isNotBlank(event.getDoi()))) {
                work.addExternalId(new ExternalId(ExternalIdType.DOI.getName(), event.getDoi(), DOI_URL.replace("##ID##", event.getDoi()), "SELF"));
            }
        }

        if (contributionRole == ContributionRole.BOTH) {
            work.addContributor(new WorkContributor(new ContributorAttributes(ContributorAttributes.ContributorSequence.FIRST, ContributorAttributes.ContributorRole.AUTHOR)));
            work.addContributor(new WorkContributor(new ContributorAttributes(ContributorAttributes.ContributorSequence.ADDITIONAL, ContributorAttributes.ContributorRole.ASSIGNEE)));
        } else if (contributionRole == ContributionRole.AUTHORED) {
            work.addContributor(new WorkContributor(new ContributorAttributes(ContributorAttributes.ContributorSequence.FIRST, ContributorAttributes.ContributorRole.AUTHOR)));
        } else {
            work.addContributor(new WorkContributor(new ContributorAttributes(ContributorAttributes.ContributorSequence.ADDITIONAL, ContributorAttributes.ContributorRole.ASSIGNEE)));
        }
        return work;
    }

    private void execute(OrcidToken tokenSession, WorkBulk workBulk, WorkBulkResponse workBulkResponse) throws IOException, OrcidOAuthException, WorkClaimException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(getSSLConnectionManager()).build();
//        HttpClient httpclient = HttpClientBuilder.create().build();  // the http-client, that will send the request
        HttpPost httpPost = new HttpPost(ORCID_API_URI + ORCID_API_VERSION + ORCID_ALL_WORKS.replace("##ORCID##", tokenSession.getOrcid()));
        httpPost.setHeader("Content-Type", "application/orcid+json; qs=4");
        httpPost.setHeader("Accept", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + tokenSession.getAccessToken()); // add the authorization header to the request

        httpPost.setEntity(new StringEntity(unmarshaller(workBulk)));
        HttpResponse response = httpclient.execute(httpPost); // the client executes the request and gets a response
        ObjectMapper mm = new ObjectMapper();
        if (response.getStatusLine().getStatusCode() == 200) {
            String output = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            WorkBulkResponse wbr = mm.readValue(output, WorkBulkResponse.class);
            workBulkResponse.getBulk().addAll(wbr.getBulk());
            workBulkResponse.getErrors().addAll(wbr.getErrors());
        } else if (response.getStatusLine().getStatusCode() == 401) {
            String output = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            OrcidToken orcidToken = mm.readValue(output, OrcidToken.class);
            throw new OrcidOAuthException("HTTP Not Authorised {401}", orcidToken);
        } else {
            String output = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            ResponseError responseError = mm.readValue(output, ResponseError.class);
            throw new WorkClaimException("Unexpected error from the API in orcid.org", responseError);
        }
    }

    public int bulkPostWork(OrcidToken tokenSession, Collection<SimpleEventProjection> events, ContributionRole contributionRole, WorkBulkResponse workBulkResponse) throws IOException, WorkClaimException, OrcidOAuthException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        int totalExecuted = 0;
        WorkBulk workBulk = new WorkBulk();
        List<Work> bulkWork = new ArrayList<>(MAX_BULK_POST);
        for (SimpleEventProjection event : events) {
            Work work = createWork(event, contributionRole);
            bulkWork.add(work);

            if (bulkWork.size() == MAX_BULK_POST) {
                totalExecuted += bulkWork.size();
                workBulk.setBulk(bulkWork);
                execute(tokenSession, workBulk, workBulkResponse);
                bulkWork = new ArrayList<>(MAX_BULK_POST);
            }
        }

        // execute the remaining works
        if (bulkWork.size() > 0) {
            totalExecuted += bulkWork.size();
            workBulk.setBulk(bulkWork);
            execute(tokenSession, workBulk, workBulkResponse);
        }

        return totalExecuted;
    }

    public Works getAllWorks(OrcidToken tokenSession) throws IOException, WorkClaimException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Works ret;
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(getSSLConnectionManager()).build();
        //HttpClient httpclient = HttpClientBuilder.create().build();  // the http-client, that will send the request
        HttpGet httpGet = new HttpGet(ORCID_API_URI + ORCID_API_VERSION + ORCID_ALL_WORKS.replace("##ORCID##", tokenSession.getOrcid()));
        httpGet.setHeader("Content-Type", "application/orcid+json; qs=4");
        httpGet.setHeader("Accept", "application/json");
        httpGet.addHeader("Authorization", "Bearer " + tokenSession.getAccessToken()); // add the authorization header to the request

        HttpResponse response = httpclient.execute(httpGet); // the client executes the request and gets a response
        if (response.getStatusLine().getStatusCode() == 200) {
            ObjectMapper mm = new ObjectMapper();
            String output = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            ret = mm.readValue(output, Works.class);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String output = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            ResponseError responseError = mapper.readValue(output, ResponseError.class);
            throw new WorkClaimException("Unexpected error from the API in orcid.org", responseError);
        }

        return ret;
    }

    public OrcidToken getAuthorisedOrcidUser(HttpServletRequest request) throws OrcidAuthorisationException {
        OrcidToken tokenSession = (OrcidToken) request.getSession().getAttribute(ORCID_TOKEN);
        if (tokenSession == null) throw new OrcidAuthorisationException("Not authorised");
        return tokenSession;
    }

    public String unmarshaller(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(obj);
    }

    public <T> List<T> marshaller(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<List<T>>() {
        });
    }

    /**
     * Hostname is used in the redirect_uri on the authorisation flow.
     * Our servers must be registered in Orcid API.
     */
    public String getHostname(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String protocol = "https://";
        if (!url.contains("reactome.org")) protocol = "http://";
        return url.replace("http://", protocol).replace(request.getRequestURI(), "");
    }

    public PoolingHttpClientConnectionManager getSSLConnectionManager() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
//        try {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new String[]
                {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);

        return new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslConnectionSocketFactory).build());
//        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
//            return null;
//
//        }
    }
}
