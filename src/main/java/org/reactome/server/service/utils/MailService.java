package org.reactome.server.service.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(final String fromName, final String fromAddress, final String toAddress, final String subject, final String msgBody) {
        this.send(fromName, fromAddress, toAddress, subject, msgBody, false);
    }

    public void send(final String fromName, final String fromAddress, final String toAddress, final String subject, final String msgBody, final Boolean sendEmailCopy) {
        try {
            MimeMessagePreparator preparator = mimeMessage -> {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
                InternetAddress from = new InternetAddress(fromAddress);
                if (StringUtils.isNotBlank(fromName)) {
                    from = new InternetAddress(fromAddress, fromName);
                }
                if (sendEmailCopy) {
                    mimeMessage.setRecipient(Message.RecipientType.BCC, new InternetAddress(fromAddress));
                }
                mimeMessage.setFrom(from);
                mimeMessage.setSubject(subject);
                mimeMessage.setText(msgBody);
            };
            mailSender.send(preparator);
        } catch (Exception e) {
            logger.error("[MAILSRVErr] The email could not be sent [To: " + toAddress + " From: " + fromAddress + " Subject: " + subject);
        }
    }
}
