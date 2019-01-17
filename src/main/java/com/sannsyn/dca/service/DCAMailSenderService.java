package com.sannsyn.dca.service;

import com.sannsyn.dca.util.DCAConfigProperties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.sannsyn.dca.util.DCAConfigProperties.getMailSenderPassword;
import static com.sannsyn.dca.util.DCAConfigProperties.getMailSenderUserName;

/**
 * An email sender class
 * Created by jobaer on 8/11/16.
 */
public class DCAMailSenderService {
    public Status sendEmail(List<String> recipients, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.googlemail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getMailSenderUserName(), getMailSenderPassword());
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sannsyn.com@gmail.com"));
            String addresses = recipients.stream().reduce("", (a, b) -> a + "," + b);
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(addresses));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Mail sent successfully");
            return Status.SUCCESS;
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
            return Status.FAILURE;
        }
    }

    public static void main(String[] args) {
        DCAMailSenderService mailSender = new DCAMailSenderService();
        mailSender.sendEmail(Collections.singletonList("jobaer@cefalo.com"), "Test message", "This is a test email. Please ignore.");
    }
}