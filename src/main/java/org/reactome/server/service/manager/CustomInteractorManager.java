package org.reactome.server.service.manager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;
import org.reactome.server.interactors.exception.CustomPsicquicInteractionClusterException;
import org.reactome.server.interactors.model.CustomPsicquicResource;
import org.reactome.server.interactors.model.Interaction;
import org.reactome.server.interactors.model.InteractionDetails;
import org.reactome.server.interactors.model.Interactor;
import org.reactome.server.interactors.service.PsicquicService;
import org.reactome.server.interactors.tuple.custom.CustomResource;
import org.reactome.server.interactors.tuple.exception.ParserException;
import org.reactome.server.interactors.tuple.model.CustomInteraction;
import org.reactome.server.interactors.tuple.model.TupleResult;
import org.reactome.server.interactors.tuple.util.ParserUtils;
import org.reactome.server.service.exception.RequestEntityTooLargeException;
import org.reactome.server.service.exception.TokenNotFoundException;
import org.reactome.server.service.exception.UnprocessableEntityException;
import org.reactome.server.service.exception.UnsupportedMediaTypeException;
import org.reactome.server.service.utils.TupleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomInteractorManager {

    private static final Logger logger = LoggerFactory.getLogger("errorLogger");

    @Autowired
    PsicquicService psicquicService;

    @Autowired
    CommonsMultipartResolver multipartResolver;

    @Autowired
    TupleManager tupleManager;

    private TupleResult getUserDataContainer(String name, String filename, String file) throws ParserException {
        try {
            String raw = name + file;
            String token = DigestUtils.md5DigestAsHex(raw.getBytes());
            TupleResult result = (TupleResult) tupleManager.readToken(token);
            if (result == null) {
                InputStream is = IOUtils.toInputStream(file, Charset.defaultCharset());
                //We only parse the data the first time it is sent
                result = ParserUtils.getUserDataContainer(name, filename, is);
                result.getSummary().setToken(token);
                tupleManager.saveToken(token, result);
            }
            return result;
        } catch (IOException | ClassCastException e) {
            throw new UnprocessableEntityException(); //TODO: Place the right exception here
        }
    }

    public TupleResult getUserDataContainerFromContent(String name, String input) throws ParserException {
        return getUserDataContainer(name, null, input);
    }

    public TupleResult getUserDataContainerFromFile(String name, MultipartFile file) throws ParserException {
        if (!file.isEmpty()) {
            try {
                String mimeType = detectMimeType(TikaInputStream.get(file.getInputStream()));
                if (!isAcceptedContentType(mimeType)) {
                    throw new UnsupportedMediaTypeException();
                }

                try {
                    return getUserDataContainer(name, file.getOriginalFilename(), IOUtils.toString(file.getInputStream(),  StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UnprocessableEntityException();
                }
            } catch (IOException | NoSuchMethodError e) {
                throw new UnsupportedMediaTypeException();
            }
        }
        throw new UnsupportedMediaTypeException();
    }

    public TupleResult getUserDataContainerFromURL(String name, String filename, String url) throws ParserException {
        if (url != null && !url.isEmpty()) {
            InputStream is;
            try {
                // Check in the URL if the filename is encoded
                String decodeFilename = URLDecoder.decode(filename, "UTF-8");
                boolean encoded = !filename.equals(decodeFilename);

                if (!encoded) {
                    String encodeFilename = URLEncoder.encode(filename, "UTF-8");
                    encodeFilename = encodeFilename.replaceAll("\\+", "%20");
                    url = url.replace(filename, encodeFilename);
                }

                HttpURLConnection conn;
                URL aux = new URL(url);

                if (aux.getProtocol().contains("https")) {
                    doTrustToCertificates(); //accepting the certificate by default
                    HttpsURLConnection tmpConn = (HttpsURLConnection) aux.openConnection();
                    is = tmpConn.getInputStream();
                    conn = tmpConn;
                } else {
                    URLConnection tmpConn = aux.openConnection();
                    is = tmpConn.getInputStream();
                    conn = (HttpURLConnection) tmpConn;
                }

                if (conn.getContentLength() > multipartResolver.getFileUpload().getSizeMax()) {
                    throw new RequestEntityTooLargeException();
                }

                String mimeType = detectMimeType(TikaInputStream.get(aux));
                if (!isAcceptedContentType(mimeType)) {
                    throw new UnsupportedMediaTypeException();
                }

            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new UnprocessableEntityException();
            }

            try {
                return getUserDataContainer(name, filename, IOUtils.toString(is,  StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UnprocessableEntityException();
            }
        }
        throw new UnsupportedMediaTypeException();
    }

    public CustomPsicquicResource registryCustomPsicquic(String name, String psicquicURL) throws ParserException {
        //InputStream is;
        try {
            URL aux = new URL(psicquicURL);
            if (aux.getProtocol().contains("https")) {
                doTrustToCertificates(); //accepting the certificate by default
            } else {
                aux.openConnection();
            }
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new UnprocessableEntityException();
        }
        CustomPsicquicResource cpr = ParserUtils.processCustomPsicquic(name, psicquicURL);
        tupleManager.saveToken(cpr.getSummary().getToken(), cpr);
        return cpr;
    }

    /**
     * Accepts certificate in case user provides a https url
     * REMARKED: Copied from Analysis project.
     */
    private void doTrustToCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        Security.addProvider(Security.getProvider("SunJSSE"));
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = (urlHostName, session) -> {
            if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                logger.warn("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
            }
            return true;
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    /**
     * Gets CustomInteraction set and convert it into Interactions
     */
    private List<Interaction> convertCustomInteraction(String searchTerm, Set<CustomInteraction> customInteractionSet) {
        List<Interaction> interactions = new ArrayList<>(customInteractionSet.size());

        for (CustomInteraction customInteraction : customInteractionSet) {
            Interaction interaction = new Interaction();

            // create interactor A
            Interactor interactorA = new Interactor();
            interactorA.setAcc(customInteraction.getInteractorIdA());
            interactorA.setAlias(customInteraction.getInteractorAliasA());

            // create interactor B
            Interactor interactorB = new Interactor();
            interactorB.setAcc(customInteraction.getInteractorIdB());
            interactorB.setAlias(customInteraction.getInteractorAliasB());

            // keep the search term, always in side A
            if (searchTerm.equals(interactorA.getAcc())) {
                interaction.setInteractorA(interactorA);
                interaction.setInteractorB(interactorB);
            } else {
                interaction.setInteractorA(interactorB);
                interaction.setInteractorB(interactorA);
            }

            // set score
            if (customInteraction.getConfidenceValue() != null) {
                interaction.setIntactScore(customInteraction.getConfidenceValue());
            }

            // set evidences list
            if (customInteraction.getEvidence() != null && customInteraction.getEvidence().size() > 0) {
                for (String evidence : customInteraction.getEvidence()) {
                    interaction.addInteractionDetails(new InteractionDetails(evidence));
                }
            }

            // add into interactions list
            interactions.add(interaction);
        }

        Collections.sort(interactions);
        Collections.reverse(interactions);

        return interactions;
    }

    /**
     * Of a given token and (list) proteins retrieve all interactions.
     * The token is associated to a data file previously submitted.
     *
     * @return for a given pr
     */
    public Map<String, List<Interaction>> getInteractionsByTokenAndProteins(String tokenStr, Set<String> proteins) throws CustomPsicquicInteractionClusterException {
        /*
         * Check if token exists in the Repository.
         */
        Object token = tupleManager.readToken(tokenStr);
        if (token != null) {
            if (token instanceof TupleResult) {
                TupleResult tupleResult = (TupleResult) token;
                return getInteractorFromCustomResource(tupleResult.getCustomResource(), proteins);
            } else {
                CustomPsicquicResource customResource = (CustomPsicquicResource) token;
                return getInteractorFromCustomPsicquic(customResource.getUrl(), proteins);
            }
        }
        throw new TokenNotFoundException(tokenStr);
    }

    private Map<String, List<Interaction>> getInteractorFromCustomResource(CustomResource customResource, Set<String> proteins) {
        Map<String, List<Interaction>> interactionMap = new HashMap<>();
        for (String singleAccession : proteins) {
            Set<CustomInteraction> customInteractionSet = new HashSet<>();
            // Check if singleAccession contains in A or B in the Interaction List
            //Ok, interacts with something.
            customInteractionSet.addAll(customResource.get(singleAccession).stream().filter(cust -> singleAccession.equals(cust.getInteractorIdA()) ||
                    singleAccession.equals(cust.getInteractorIdB())).collect(Collectors.toList()));
            List<Interaction> interactions = convertCustomInteraction(singleAccession, customInteractionSet);
            interactionMap.put(singleAccession, interactions);
        }
        return interactionMap;
    }

    private Map<String, List<Interaction>> getInteractorFromCustomPsicquic(String url, Set<String> proteins) throws CustomPsicquicInteractionClusterException {
        Map<String, List<Interaction>> interactionMap;
        interactionMap = psicquicService.getInteractionFromCustomPsicquic(url, proteins);
        return interactionMap;
    }

    /**
     * Retrieve the filename of a given url
     *
     * @return String as filename
     */
    public String getFileNameFromURL(String url) {
        String name = "";
        if (url != null && !url.isEmpty()) {
            try {
                name = FilenameUtils.getName((new URL(url)).getFile());
            } catch (MalformedURLException e) {
                /*Nothing here*/
            }
        }
        return name;
    }

    /**
     * Detect MimeType using apache tika.
     * jMimeMagic has failed when analysing the PSIMITAB .txt file export from IntAct page
     *
     * @throws IOException
     */
    private String detectMimeType(TikaInputStream tikaInputStream) throws IOException {
        final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());

        try {
            return DETECTOR.detect(tikaInputStream, new Metadata()).toString();
        } finally {
            if (tikaInputStream != null) {
                tikaInputStream.close();
            }
        }
    }

    private boolean isAcceptedContentType(String contentType) {
        return contentType.contains("text/plain") || contentType.contains("text/csv");
    }
}