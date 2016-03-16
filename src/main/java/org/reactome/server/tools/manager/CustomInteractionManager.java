package org.reactome.server.tools.manager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.reactome.server.tools.exception.*;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.InteractionDetails;
import org.reactome.server.tools.interactors.model.Interactor;
import org.reactome.server.tools.interactors.tuple.exception.ParserException;
import org.reactome.server.tools.interactors.tuple.exception.TupleParserException;
import org.reactome.server.tools.interactors.tuple.model.CustomInteraction;
import org.reactome.server.tools.interactors.tuple.model.Summary;
import org.reactome.server.tools.interactors.tuple.model.UserDataContainer;
import org.reactome.server.tools.interactors.tuple.token.Token;
import org.reactome.server.tools.interactors.tuple.util.ParserUtils;
import org.reactome.server.tools.interactors.util.Toolbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
public class CustomInteractionManager {

    private static Logger logger = Logger.getLogger(CustomInteractionManager.class);

    public static Map<Token, Summary> tokenMap = new HashMap<>();

    @Autowired
    CommonsMultipartResolver multipartResolver;

    public Summary getUserDataContainer(InputStream is) {
        Summary summary = null;
        try {
            summary = ParserUtils.getUserDataContainer(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TupleParserException e) {
            throw new DataFormatException(e.getErrorMessages());
        } catch (ParserException e) {
            throw new DataFormatException(e.getMessage());
        }

        return summary;
    }

    public Summary getUserDataContainer(String input) {
        Summary summary = null;
        try {
            summary = ParserUtils.getUserDataContainer(IOUtils.toInputStream(input));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TupleParserException e) {
            throw new DataFormatException(e.getErrorMessages());
        } catch (ParserException e) {
            throw new DataFormatException(e.getMessage());
        }

        return summary;
    }

    public Summary getUserDataContainerFromURL(String url) {
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
                return ParserUtils.getUserDataContainer(is);
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

    private boolean isAcceptedContentType(String contentType) {
        return contentType.contains("text/plain") || contentType.contains("text/csv");
    }

    /**
     * Copied from analysis project.
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
    private List<Interaction> converteCustomInteraction(Set<CustomInteraction> customInteractionSet) {
        List<Interaction> interactions = new ArrayList<>(customInteractionSet.size());

        for (CustomInteraction customInteraction : customInteractionSet) {
            Interaction interaction = new Interaction();

            Interactor interactorA = new Interactor();
            interactorA.setAcc(customInteraction.getInteractorIdA());
            interactorA.setAlias(customInteraction.getAliasInteractorA());
            if (StringUtils.isNotEmpty(customInteraction.getTaxonomyIdInteractorA())) {
                interactorA.setTaxid(Integer.parseInt(customInteraction.getTaxonomyIdInteractorA()));
            }
            interactorA.setSynonyms(customInteraction.getAlternativeInteractorA());

            Interactor interactorB = new Interactor();
            interactorB.setAcc(customInteraction.getInteractorIdB());
            interactorB.setAlias(customInteraction.getAliasInteractorB());
            if (StringUtils.isNotEmpty(customInteraction.getTaxonomyIdInteractorB())) {
                interactorB.setTaxid(Integer.parseInt(customInteraction.getTaxonomyIdInteractorB()));
            }
            interactorB.setSynonyms(customInteraction.getAlternativeInteractorB());

            interaction.setInteractorA(interactorA);
            interaction.setInteractorB(interactorB);

            if (StringUtils.isNotEmpty(customInteraction.getConfidenceValue())) {
                if (Toolbox.isNumeric(customInteraction.getConfidenceValue())) {
                    interaction.setIntactScore(Double.parseDouble(customInteraction.getConfidenceValue()));
                }
            }

            if (StringUtils.isNotEmpty(customInteraction.getInteractionIdentifier())) {
                InteractionDetails evidences = new InteractionDetails();
                evidences.setInteractionAc(customInteraction.getInteractionIdentifier());

                interaction.addInteractionDetails(evidences);
            }

            interactions.add(interaction);

        }

        return interactions;

    }


    /**
     * Of a given token and (list) proteins retrieve all interactions.
     * The token is associated to a data file previously submitted.
     *
     * @return for a given pr
     */
    public Map<String, List<Interaction>> getInteractionsByTokenAndProteins(String tokenStr, Set<String> proteins) {
        Map<String, List<Interaction>> interactionMap = new HashMap<>();

        // The return can be a list of something
        Set<CustomInteraction> customInteractionSet = new HashSet<>();


        Token token = new Token(tokenStr);

        if (!tokenMap.containsKey(token)) {
            throw new TokenNotFoundException();
        }

        //if(!TokenUtil.isValid(token)) {
        //throw new TokenExpiredException();
        //}

        /** Retrieve stored summary associated with given token **/
        Summary summary = tokenMap.get(token);

        /** Get user data **/
        UserDataContainer data = summary.getData();

        /** Get custom interactions **/
        Set<CustomInteraction> allInteractions = data.getCustomInteractions();

        for (String singleAccession : proteins) {
            // Check if singleAccession contains in A or B in the Interaction List
            for (CustomInteraction cust : allInteractions) {
                if (singleAccession.equals(cust.getInteractorIdA()) ||
                        singleAccession.equals(cust.getInteractorIdB())) {

                    //Ok, interacts with something.
                    customInteractionSet.add(cust);
                }
            }

            List<Interaction> interactions = converteCustomInteraction(customInteractionSet);
            interactionMap.put(singleAccession, interactions);

        }

        return interactionMap;
    }

    public void saveToken(Summary summary) {
        tokenMap.put(summary.getToken(), summary);
    }

}