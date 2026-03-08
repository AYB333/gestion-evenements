package com.ayoub.gestionevenements.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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
        MailConfig config = MailConfig.load();
        if (!config.enabled()) {
            writeToFile(to, subject, html);
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
            message.setContent(html, "text/html; charset=UTF-8");
            Transport.send(message);
        } catch (MessagingException ex) {
            writeToFile(to, subject, html);
        }
    }

    private void writeToFile(String to, String subject, String html) {
        try {
            Path outDir = Path.of(System.getProperty("java.io.tmpdir"), "gestion-evenements-mails");
            Files.createDirectories(outDir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String safeTo = to == null ? "unknown" : to.replaceAll("[^a-zA-Z0-9@._-]", "_");
            Path outFile = outDir.resolve("mail-" + safeTo + "-" + stamp + ".html");
            String content = "<!-- Mock email -->\n" + html;
            Files.writeString(outFile, content, StandardCharsets.UTF_8);
            System.out.println("Mock email saved to: " + outFile.toAbsolutePath());
        } catch (IOException ignored) {
            System.out.println("Email delivery failed and could not write mock email.");
        }
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
