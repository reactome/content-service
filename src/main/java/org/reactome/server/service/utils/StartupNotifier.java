package org.reactome.server.service.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@Component
public class StartupNotifier extends Thread {

    private static Logger logger = LoggerFactory.getLogger("threadLogger");

    private static final String PROJECT = "ContentService";
    private static final String SENDER_NAME = "Tomcat";

    private MailService sms;
    private String from;
    private String to;

    @Autowired
    public StartupNotifier(MailService sms,
                           @Value("${startup.notification.from}")
                                   String from,
                           @Value("${startup.notification.to}")
                                   String to,
                           @Value("${startup.notification}")
                                   String notify) {
        super("DC-StartupNotifier");
        if (Boolean.valueOf(notify) && to != null) {
            this.sms = sms;
            this.from = from;
            this.to = to;
            start();
        }
    }

    @Override
    public void run() {
        try {
            logger.debug("Getting ready to send an email....");

            final String serverName = InetAddress.getLocalHost().getHostName();
            final String subject = "[" + serverName + "] " + PROJECT + " deployed " + getTimestamp();
            String body = PROJECT + " has been (re)deployed in [" + serverName + "]";
            body += "\n\n List of who is logged in: \n";
            body += getWho();

            sms.start(SENDER_NAME, from, to, subject, body);

            logger.debug("Sent!");
        } catch (Exception e) {
            logger.warn("Startup notification failed because: " + e.getMessage().split("\n")[0]);
        }
    }

    private String getTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private String getWho() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("who");
        Process process = pb.start();
        return process.waitFor() == 0 ? IOUtils.toString(process.getInputStream(), Charset.defaultCharset()) : "<<Couldn't execute 'who' command>>";
    }
}
