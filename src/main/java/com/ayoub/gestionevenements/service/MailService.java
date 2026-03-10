package com.ayoub.gestionevenements.service;

import jakarta.activation.DataHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.BodyPart;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@ApplicationScoped
public class MailService {

    public void sendHtml(String to, String subject, String html) {
        sendHtml(to, subject, html, new MailAttachment[0]);
    }

    public void sendHtml(String to, String subject, String html, MailAttachment... attachments) {
        MailConfig config = MailConfig.load();
        if (!config.enabled()) {
            // Demo fallback: keep a local HTML copy when no SMTP server is configured.
            writeToFile(to, subject, html, attachments);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));
        props.put("mail.smtp.auth", config.user() != null && !config.user().isBlank());
        props.put("mail.smtp.starttls.enable", String.valueOf(config.tls()));

        Session session = Session.getInstance(props, config.user() == null ? null : new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.user(), config.password());
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.from()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            if (attachments != null && attachments.length > 0) {
                MimeMultipart multipart = new MimeMultipart();

                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(html, "text/html; charset=UTF-8");
                multipart.addBodyPart(htmlPart);

                for (MailAttachment attachment : attachments) {
                    if (attachment == null || attachment.content() == null || attachment.content().length == 0) {
                        continue;
                    }
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.setDataHandler(new DataHandler(
                            new ByteArrayDataSource(attachment.content(), attachment.contentType())));
                    attachmentPart.setFileName(attachment.fileName());
                    multipart.addBodyPart(attachmentPart);
                }

                message.setContent(multipart);
            } else {
                message.setContent(html, "text/html; charset=UTF-8");
            }
            Transport.send(message);
        } catch (MessagingException ex) {
            writeToFile(to, subject, html, attachments);
        }
    }

    private void writeToFile(String to, String subject, String html, MailAttachment... attachments) {
        try {
            Path outDir = Path.of(System.getProperty("java.io.tmpdir"), "gestion-evenements-mails");
            Files.createDirectories(outDir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String safeTo = to == null ? "unknown" : to.replaceAll("[^a-zA-Z0-9@._-]", "_");
            Path outFile = outDir.resolve("mail-" + safeTo + "-" + stamp + ".html");
            String content = "<!-- Mock email -->\n" + html;
            Files.writeString(outFile, content, StandardCharsets.UTF_8);
            if (attachments != null) {
                for (MailAttachment attachment : attachments) {
                    if (attachment == null || attachment.content() == null || attachment.content().length == 0) {
                        continue;
                    }
                    Path attachmentFile = outDir.resolve(
                            "mail-" + safeTo + "-" + stamp + "-" + sanitizeFileName(attachment.fileName()));
                    Files.write(attachmentFile, attachment.content());
                }
            }
            System.out.println("Mock email saved to: " + outFile.toAbsolutePath());
        } catch (IOException ignored) {
            System.out.println("Email delivery failed and could not write mock email.");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment.bin";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record MailAttachment(String fileName, String contentType, byte[] content) {
    }

    private record MailConfig(String host, int port, String user, String password, String from, boolean tls) {
        static MailConfig load() {
            String host = getenvOrDefault("SMTP_HOST", "");
            if (host.isBlank()) {
                return new MailConfig("", 0, null, null, "no-reply@gestion-evenements.local", false);
            }
            int port = parseInt(getenvOrDefault("SMTP_PORT", "587"), 587);
            String user = getenvOrDefault("SMTP_USER", "");
            String pass = getenvOrDefault("SMTP_PASS", "");
            String from = getenvOrDefault("SMTP_FROM", "no-reply@gestion-evenements.local");
            boolean tls = Boolean.parseBoolean(getenvOrDefault("SMTP_TLS", "true"));
            return new MailConfig(host, port, user, pass, from, tls);
        }

        boolean enabled() {
            return host != null && !host.isBlank();
        }

        private static String getenvOrDefault(String key, String def) {
            String value = System.getenv(key);
            return value == null ? def : value;
        }

        private static int parseInt(String value, int def) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return def;
            }
        }
    }
}
