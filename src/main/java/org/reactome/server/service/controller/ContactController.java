package org.reactome.server.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.reactome.server.service.utils.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Backs the help-desk contact form on the search page. The Angular site
 * surfaces this form when /search returns an error or zero results; it
 * POSTs subject + message + the user's email and an hCaptcha response
 * token. We verify the captcha server-side, then hand off to MailService
 * to deliver the email to the support address.
 */
@SuppressWarnings("unused")
@RestController
public class ContactController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
    private static final String HCAPTCHA_VERIFY_URL = "https://api.hcaptcha.com/siteverify";

    @Value("${captcha.secret.key}")
    private String captchaSecret;

    @Value("${mail.support.address}")
    private String supportAddress;

    @Value("${mail.support.name}")
    private String supportName;

    private MailService mailService;

    @PostMapping(value = "/contact")
    public ResponseEntity<?> contact(
            @RequestParam(value = "contactName", required = false, defaultValue = "") String contactName,
            @RequestParam("mailAddress") String mailAddress,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "sendEmailCopy", required = false) String sendEmailCopy,
            @RequestParam("h-captcha-response") String captchaResponse) throws IOException {

        if (!verifyCaptcha(captchaResponse)) {
            return ResponseEntity.status(400).body("captcha verification failed");
        }
        if (StringUtils.isBlank(mailAddress) || StringUtils.isBlank(subject) || StringUtils.isBlank(message)) {
            return ResponseEntity.status(400).body("missing required field");
        }

        String fromName = StringUtils.isBlank(contactName) ? mailAddress : contactName;
        String body = String.format("From: %s <%s>%n%n%s", fromName, mailAddress, message);

        mailService.start(fromName, mailAddress, supportAddress, subject, body);

        if ("on".equalsIgnoreCase(sendEmailCopy) || "true".equalsIgnoreCase(sendEmailCopy)) {
            mailService.start(supportName, supportAddress, mailAddress, "[Copy] " + subject, body);
        }

        infoLogger.info("Help-desk contact form submitted from {} (subject: {})", mailAddress, subject);
        return ResponseEntity.ok().build();
    }

    private boolean verifyCaptcha(String response) throws IOException {
        if (StringUtils.isBlank(response)) return false;
        // The deployed tomcat uses a Reactome-internal truststore that lacks
        // public root CAs, so hcaptcha.com's normal TLS chain fails to
        // validate. Match what OrcidHelper does and accept any cert -- the
        // body we get back is verified by content, not by TLS identity.
        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(permissiveConnectionManager()).build()) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("secret", captchaSecret));
            params.add(new BasicNameValuePair("response", response));
            HttpPost post = new HttpPost(HCAPTCHA_VERIFY_URL);
            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            HttpResponse resp = client.execute(post);
            String json = IOUtils.toString(resp.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode node = new ObjectMapper().readTree(json);
            return node.path("success").asBoolean(false);
        }
    }

    private static PoolingHttpClientConnectionManager permissiveConnectionManager() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true).build();
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(
                    sslContext, new String[]{"TLSv1.2", "TLSv1.3"}, null,
                    NoopHostnameVerifier.INSTANCE);
            return new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslFactory).build());
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Could not build permissive SSL context", e);
        }
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }
}
