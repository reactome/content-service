package org.reactome.server.tools.manager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.reactome.server.tools.exception.*;
import org.reactome.server.tools.interactors.exception.CustomPsicquicInteractionClusterException;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.InteractionDetails;
import org.reactome.server.tools.interactors.model.Interactor;
import org.reactome.server.tools.interactors.service.PsicquicService;
import org.reactome.server.tools.interactors.tuple.exception.ParserException;
import org.reactome.server.tools.interactors.tuple.exception.TupleParserException;
import org.reactome.server.tools.interactors.tuple.model.*;
import org.reactome.server.tools.interactors.tuple.util.ParserUtils;
import org.reactome.server.tools.interactors.util.InteractorConstant;
import org.reactome.server.tools.interactors.util.Toolbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Controller
public class CustomInteractorManager {

    private static Logger logger = Logger.getLogger(CustomInteractorManager.class);

    @Autowired
    PsicquicService psicquicService;

    @Autowired
    CommonsMultipartResolver multipartResolver;

    public TupleResult getUserDataContainer(String name, String filename, InputStream is) {
        TupleResult result = null;
        try {
            result = ParserUtils.getUserDataContainer(name, filename, is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TupleParserException e) {
            throw new DataFormatException(e.getErrorMessages());
        } catch (ParserException e) {
            throw new DataFormatException(e.getMessage());
        }

        return result;
    }

    public TupleResult getUserDataContainer(String name, String filename, String input) {
        return getUserDataContainer(name, filename, IOUtils.toInputStream(input));
    }

    public TupleResult getUserDataContainerFromURL(String name, String filename, String url) {
        if (url != null && !url.isEmpty()) {
            InputStream is;
            try {
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

                if (!isAcceptedContentType(conn.getContentType())) {
                    throw new UnsupportedMediaTypeException();
                }

            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                throw new UnprocessableEntityException();
            }


            try {
                return ParserUtils.getUserDataContainer(name, filename, is);
            } catch (IOException e) {
                throw new UnsupportedMediaTypeException();
            } catch (TupleParserException e) {
                throw new DataFormatException(e.getErrorMessages());
            } catch (ParserException e) {
                throw new DataFormatException(e.getMessage());
            }
        }

        throw new UnsupportedMediaTypeException();
    }

    public TupleResult registryCustomPsicquic(String name, String psicquicURL) {
        //InputStream is;
        try {
            HttpURLConnection conn;
            URL aux = new URL(psicquicURL);
            if (aux.getProtocol().contains("https")) {
                doTrustToCertificates(); //accepting the certificate by default
                conn = (HttpsURLConnection) aux.openConnection();
                //conn = tmpConn;
            } else {
                URLConnection tmpConn = aux.openConnection();
                conn = (HttpURLConnection) tmpConn;
            }

            // THIS VALIDATION IS NOT WORKING. PSIQUIC Service does not have landing page, then it returns 404
            /*if (conn.getResponseCode() != HttpStatus.OK.value()) {
                throw new UnreachableException(conn.getResponseCode());
            }*/

        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new UnprocessableEntityException();
        }

        try {
            return ParserUtils.processCustomPsicquic(name, psicquicURL);
        } catch (TupleParserException e) {
            throw new DataFormatException(e.getErrorMessages());
        } catch (ParserException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    private boolean isAcceptedContentType(String contentType) {
        return contentType.contains("text/plain") || contentType.contains("text/csv");
    }

    /**
     * Accepts certificate in case user provides a https url
     * REMARKED: Copied from Analysis project.
     */
    private void doTrustToCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
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
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    logger.warn("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    /**
     * Gets CustomInteraction set and convert it into Interactions
     */
    private List<Interaction> converteCustomInteraction(String searchTerm, Set<CustomInteraction> customInteractionSet) {
        List<Interaction> interactions = new ArrayList<>(customInteractionSet.size());

        for (CustomInteraction customInteraction : customInteractionSet) {
            Interaction interaction = new Interaction();

            /** create interactor A **/
            Interactor interactorA = new Interactor();
            interactorA.setAcc(customInteraction.getInteractorIdA());
            interactorA.setAlias(customInteraction.getAliasInteractorA());
            if (StringUtils.isNotEmpty(customInteraction.getTaxonomyIdInteractorA())) {
                interactorA.setTaxid(Integer.parseInt(customInteraction.getTaxonomyIdInteractorA()));
            }
            interactorA.setSynonyms(customInteraction.getAlternativeInteractorA());

            /** create interactor A **/
            Interactor interactorB = new Interactor();
            interactorB.setAcc(customInteraction.getInteractorIdB());
            interactorB.setAlias(customInteraction.getAliasInteractorB());
            if (StringUtils.isNotEmpty(customInteraction.getTaxonomyIdInteractorB())) {
                interactorB.setTaxid(Integer.parseInt(customInteraction.getTaxonomyIdInteractorB()));
            }
            interactorB.setSynonyms(customInteraction.getAlternativeInteractorB());

            /** keep the search term, always in side A **/
            if (searchTerm.equals(interactorA.getAcc())) {
                interaction.setInteractorA(interactorA);
                interaction.setInteractorB(interactorB);
            } else {
                interaction.setInteractorA(interactorB);
                interaction.setInteractorB(interactorA);
            }

            /** set score **/
            if (StringUtils.isNotEmpty(customInteraction.getConfidenceValue())) {
                if (Toolbox.isNumeric(customInteraction.getConfidenceValue())) {
                    interaction.setIntactScore(Double.parseDouble(customInteraction.getConfidenceValue()));
                }
            }

            /** set evidences list **/
            if (StringUtils.isNotEmpty(customInteraction.getInteractionIdentifier())) {
                InteractionDetails evidences = new InteractionDetails();
                evidences.setInteractionAc(customInteraction.getInteractionIdentifier());

                interaction.addInteractionDetails(evidences);
            }

            /** add into interactions list **/
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
    public Map<String, List<Interaction>> getInteractionsByTokenAndProteins(String tokenStr, Set<String> proteins) {
        Map<String, List<Interaction>> interactionMap;

        /**
         * Check if token exists in the Repository.
         */
        if (!CustomInteractorRepository.getKeys().contains(tokenStr) && !CustomPsicquicRepository.getKeys().contains(tokenStr)) {
            throw new TokenNotFoundException(tokenStr);
        }

        if (tokenStr.startsWith(InteractorConstant.TUPLE_PREFIX)) {
            interactionMap = getInteractorFromCustomPsicquic(tokenStr, proteins);
        } else {
            interactionMap = getInteractorFromCustomDataSubmission(tokenStr, proteins);
        }

        return interactionMap;
    }

    private Map<String, List<Interaction>> getInteractorFromCustomDataSubmission(String tokenStr, Set<String> proteins) {
        Map<String, List<Interaction>> interactionMap = new HashMap<>();

        Set<CustomInteraction> customInteractionSet = new HashSet<>();

        /** Retrieve stored summary associated with given token **/
        UserDataContainer udc = CustomInteractorRepository.getByToken(tokenStr);

        /** Get custom interactions **/
        Set<CustomInteraction> allInteractions = udc.getCustomInteractions();

        for (String singleAccession : proteins) {
            // Check if singleAccession contains in A or B in the Interaction List
            for (CustomInteraction cust : allInteractions) {
                if (singleAccession.equals(cust.getInteractorIdA()) ||
                        singleAccession.equals(cust.getInteractorIdB())) {

                    //Ok, interacts with something.
                    customInteractionSet.add(cust);
                }
            }

            List<Interaction> interactions = converteCustomInteraction(singleAccession, customInteractionSet);

            interactionMap.put(singleAccession, interactions);

        }

        return interactionMap;
    }

    private Map<String, List<Interaction>> getInteractorFromCustomPsicquic(String tokenStr, Set<String> proteins) {
        Map<String, List<Interaction>> interactionMap;

        /** Retrieve stored summary associated with given token **/
        String url = CustomPsicquicRepository.getByToken(tokenStr);

        try {
            interactionMap = psicquicService.getInteractionFromCustomPsicquic(url, proteins);
        } catch (CustomPsicquicInteractionClusterException e) {
            throw new PsicquicContentException("Error querying your PSICQUIC Resource.");
        }

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

}