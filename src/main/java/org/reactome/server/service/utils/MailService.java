package org.reactome.server.service.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void start(final String fromName, final String fromAddress, final String toAddress, final String subject, final String msgBody) {
        mailSender.send(mimeMessage -> {
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            InternetAddress from = new InternetAddress(fromAddress);
            if (StringUtils.isNotBlank(fromName)) {
                from = new InternetAddress(fromAddress, fromName);
            }
            mimeMessage.setFrom(from);
            mimeMessage.setSubject(subject);
            mimeMessage.setText(msgBody);
        });
    }
}
