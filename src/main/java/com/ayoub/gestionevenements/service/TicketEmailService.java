package com.ayoub.gestionevenements.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@ApplicationScoped
public class TicketEmailService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Inject
    private MailService mailService;

    @Inject
    private QrCodeService qrCodeService;

    public void sendPaymentSuccess(String email,
                                   String fullName,
                                   String eventTitle,
                                   LocalDateTime eventDate,
                                   String ticketCode,
                                   BigDecimal montant,
                                   String methode) {
        if (email == null || email.isBlank()) {
            return;
        }

        byte[] qrBytes = qrCodeService.generatePngBytes("TICKET:" + ticketCode, 260);
        String qr = qrBytes == null ? null : Base64.getEncoder().encodeToString(qrBytes);
        String dateText = eventDate == null ? "-" : DATE_FORMAT.format(eventDate);
        String html = buildHtml(fullName, eventTitle, dateText, ticketCode, montant, methode, qr);
        String subject = "Votre billet - " + eventTitle;
        if (qrBytes != null) {
            mailService.sendHtml(
                    email,
                    subject,
                    html,
                    new MailService.MailAttachment(buildQrFileName(ticketCode), "image/png", qrBytes));
            return;
        }
        mailService.sendHtml(email, subject, html);
    }

    public void sendTicketCancelledParticipant(String email,
                                               String fullName,
                                               String eventTitle,
                                               LocalDateTime eventDate,
                                               String ticketCode) {
        if (email == null || email.isBlank()) {
            return;
        }
        String dateText = eventDate == null ? "-" : DATE_FORMAT.format(eventDate);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>Billet annule</h2>");
        sb.append("<p>Bonjour ").append(escapeHtml(fullName == null ? "Participant" : fullName)).append(",</p>");
        sb.append("<p>Votre billet a bien ete annule.</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(escapeHtml(eventTitle == null ? "-" : eventTitle)).append("</li>");
        sb.append("<li><strong>Date:</strong> ").append(escapeHtml(dateText)).append("</li>");
        sb.append("<li><strong>Code:</strong> ").append(escapeHtml(ticketCode == null ? "-" : ticketCode)).append("</li>");
        sb.append("</ul>");
        sb.append("</div></body></html>");
        mailService.sendHtml(email, "Annulation de billet - " + (eventTitle == null ? "Evenement" : eventTitle), sb.toString());
    }

    public void sendTicketTransferSentParticipant(String email,
                                                  String fullName,
                                                  String eventTitle,
                                                  String targetEmail,
                                                  String ticketCode) {
        if (email == null || email.isBlank()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>Transfert confirme</h2>");
        sb.append("<p>Bonjour ").append(escapeHtml(fullName == null ? "Participant" : fullName)).append(",</p>");
        sb.append("<p>Votre billet a ete transfere vers ").append(escapeHtml(targetEmail == null ? "-" : targetEmail)).append(".</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(escapeHtml(eventTitle == null ? "-" : eventTitle)).append("</li>");
        sb.append("<li><strong>Code initial:</strong> ").append(escapeHtml(ticketCode == null ? "-" : ticketCode)).append("</li>");
        sb.append("</ul>");
        sb.append("</div></body></html>");
        mailService.sendHtml(email, "Transfert de billet - " + (eventTitle == null ? "Evenement" : eventTitle), sb.toString());
    }

    public void sendTicketTransferReceivedParticipant(String email,
                                                      String fullName,
                                                      String eventTitle,
                                                      LocalDateTime eventDate,
                                                      String ticketCode,
                                                      BigDecimal montant,
                                                      String sourceEmail,
                                                      boolean paid) {
        if (email == null || email.isBlank()) {
            return;
        }
        String dateText = eventDate == null ? "-" : DATE_FORMAT.format(eventDate);
        byte[] qrBytes = paid ? qrCodeService.generatePngBytes("TICKET:" + ticketCode, 260) : null;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>Nouveau billet recu</h2>");
        sb.append("<p>Bonjour ").append(escapeHtml(fullName == null ? "Participant" : fullName)).append(",</p>");
        sb.append("<p>Un billet vous a ete transfere par ").append(escapeHtml(sourceEmail == null ? "-" : sourceEmail)).append(".</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(escapeHtml(eventTitle == null ? "-" : eventTitle)).append("</li>");
        sb.append("<li><strong>Date:</strong> ").append(escapeHtml(dateText)).append("</li>");
        sb.append("<li><strong>Code:</strong> ").append(escapeHtml(ticketCode == null ? "-" : ticketCode)).append("</li>");
        sb.append("<li><strong>Montant:</strong> ").append(escapeHtml(montant == null ? "-" : montant + " MAD")).append("</li>");
        sb.append("<li><strong>Statut:</strong> ").append(paid ? "Paye" : "Reserve").append("</li>");
        sb.append("</ul>");
        if (qrBytes != null) {
            sb.append("<p>Le QR code de votre billet est joint a cet email en piece jointe.</p>");
        }
        sb.append("</div></body></html>");
        String subject = "Billet transfere - " + (eventTitle == null ? "Evenement" : eventTitle);
        if (qrBytes != null) {
            mailService.sendHtml(
                    email,
                    subject,
                    sb.toString(),
                    new MailService.MailAttachment(buildQrFileName(ticketCode), "image/png", qrBytes));
            return;
        }
        mailService.sendHtml(email, subject, sb.toString());
    }

    public void sendOrganizerSaleNotification(String organizerEmail,
                                              String organizerName,
                                              String participantEmail,
                                              String eventTitle,
                                              String ticketCode,
                                              BigDecimal montant,
                                              String methode) {
        sendOrganizerNotification(
                organizerEmail,
                organizerName,
                "Nouveau paiement recu",
                "Un participant a paye un billet.",
                participantEmail,
                eventTitle,
                ticketCode,
                montant,
                methode);
    }

    public void sendOrganizerReservationNotification(String organizerEmail,
                                                     String organizerName,
                                                     String participantEmail,
                                                     String participantName,
                                                     String eventTitle,
                                                     LocalDateTime eventDate,
                                                     String ticketCode,
                                                     BigDecimal montant) {
        if (organizerEmail == null || organizerEmail.isBlank()) {
            return;
        }
        String dateText = eventDate == null ? "-" : DATE_FORMAT.format(eventDate);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>Nouvelle reservation</h2>");
        sb.append("<p>Bonjour ").append(escapeHtml(organizerName == null ? "Organisateur" : organizerName)).append(",</p>");
        sb.append("<p>Un participant vient de reserver une place pour votre evenement.</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(escapeHtml(eventTitle == null ? "-" : eventTitle)).append("</li>");
        sb.append("<li><strong>Date:</strong> ").append(escapeHtml(dateText)).append("</li>");
        sb.append("<li><strong>Participant:</strong> ")
                .append(escapeHtml(participantName == null || participantName.isBlank() ? participantEmail : participantName))
                .append("</li>");
        sb.append("<li><strong>Email participant:</strong> ").append(escapeHtml(participantEmail == null ? "-" : participantEmail)).append("</li>");
        sb.append("<li><strong>Code billet:</strong> ").append(escapeHtml(ticketCode == null ? "-" : ticketCode)).append("</li>");
        if (montant != null) {
            sb.append("<li><strong>Montant reserve:</strong> ").append(escapeHtml(montant + " MAD")).append("</li>");
        }
        sb.append("<li><strong>Statut:</strong> Reserve</li>");
        sb.append("</ul>");
        sb.append("</div></body></html>");
        mailService.sendHtml(
                organizerEmail,
                "Nouvelle reservation - " + (eventTitle == null ? "Evenement" : eventTitle),
                sb.toString());
    }

    public void sendOrganizerCancellationNotification(String organizerEmail,
                                                      String organizerName,
                                                      String participantEmail,
                                                      String eventTitle,
                                                      String ticketCode) {
        sendOrganizerNotification(
                organizerEmail,
                organizerName,
                "Annulation de billet",
                "Un participant a annule son billet.",
                participantEmail,
                eventTitle,
                ticketCode,
                null,
                null);
    }

    public void sendOrganizerTransferNotification(String organizerEmail,
                                                  String organizerName,
                                                  String sourceEmail,
                                                  String targetEmail,
                                                  String eventTitle,
                                                  String oldCode,
                                                  String newCode) {
        if (organizerEmail == null || organizerEmail.isBlank()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>Transfert de billet</h2>");
        sb.append("<p>Bonjour ").append(escapeHtml(organizerName == null ? "Organisateur" : organizerName)).append(",</p>");
        sb.append("<p>Un billet a ete transfere entre participants.</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(escapeHtml(eventTitle == null ? "-" : eventTitle)).append("</li>");
        sb.append("<li><strong>Source:</strong> ").append(escapeHtml(sourceEmail == null ? "-" : sourceEmail)).append("</li>");
        sb.append("<li><strong>Destination:</strong> ").append(escapeHtml(targetEmail == null ? "-" : targetEmail)).append("</li>");
        sb.append("<li><strong>Ancien code:</strong> ").append(escapeHtml(oldCode == null ? "-" : oldCode)).append("</li>");
        sb.append("<li><strong>Nouveau code:</strong> ").append(escapeHtml(newCode == null ? "-" : newCode)).append("</li>");
        sb.append("</ul>");
        sb.append("</div></body></html>");
        mailService.sendHtml(organizerEmail, "Transfert billet - " + (eventTitle == null ? "Evenement" : eventTitle), sb.toString());
    }

    private String buildHtml(String fullName,
                             String eventTitle,
                             String eventDate,
                             String ticketCode,
                             BigDecimal montant,
                             String methode,
                             String qrBase64) {
        String safeName = escapeHtml(fullName == null ? "Participant" : fullName);
        String safeTitle = escapeHtml(eventTitle == null ? "Evenement" : eventTitle);
        String safeCode = escapeHtml(ticketCode == null ? "-" : ticketCode);
        String safeMontant = escapeHtml(montant == null ? "-" : montant + " MAD");
        String safeMethode = escapeHtml(methode == null ? "-" : methode);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>Merci pour votre paiement</h2>");
        sb.append("<p>Bonjour ").append(safeName).append(",</p>");
        sb.append("<p>Votre paiement est confirme. Voici les details de votre billet :</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(safeTitle).append("</li>");
        sb.append("<li><strong>Date:</strong> ").append(eventDate).append("</li>");
        sb.append("<li><strong>Code:</strong> ").append(safeCode).append("</li>");
        sb.append("<li><strong>Montant:</strong> ").append(safeMontant).append("</li>");
        sb.append("<li><strong>Methode:</strong> ").append(safeMethode).append("</li>");
        sb.append("</ul>");
        if (qrBase64 != null) {
            sb.append("<p>QR Code:</p>");
            sb.append("<img alt='QR Code' style='width:180px;height:180px' src='data:image/png;base64,")
                    .append(qrBase64)
                    .append("'>");
        }
        sb.append("<p style='margin-top:24px;color:#94a3b8;'>Gestion Evenements</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void sendOrganizerNotification(String organizerEmail,
                                           String organizerName,
                                           String title,
                                           String intro,
                                           String participantEmail,
                                           String eventTitle,
                                           String ticketCode,
                                           BigDecimal montant,
                                           String methode) {
        if (organizerEmail == null || organizerEmail.isBlank()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;background:#0b0f1a;color:#e2e8f0;padding:24px;'>");
        sb.append("<div style='max-width:640px;margin:0 auto;background:#111827;border-radius:16px;padding:24px;'>");
        sb.append("<h2 style='margin-top:0;color:#38bdf8;'>").append(escapeHtml(title)).append("</h2>");
        sb.append("<p>Bonjour ").append(escapeHtml(organizerName == null ? "Organisateur" : organizerName)).append(",</p>");
        sb.append("<p>").append(escapeHtml(intro)).append("</p>");
        sb.append("<ul>");
        sb.append("<li><strong>Evenement:</strong> ").append(escapeHtml(eventTitle == null ? "-" : eventTitle)).append("</li>");
        sb.append("<li><strong>Participant:</strong> ").append(escapeHtml(participantEmail == null ? "-" : participantEmail)).append("</li>");
        sb.append("<li><strong>Code billet:</strong> ").append(escapeHtml(ticketCode == null ? "-" : ticketCode)).append("</li>");
        if (montant != null) {
            sb.append("<li><strong>Montant:</strong> ").append(escapeHtml(montant + " MAD")).append("</li>");
        }
        if (methode != null && !methode.isBlank()) {
            sb.append("<li><strong>Methode:</strong> ").append(escapeHtml(methode)).append("</li>");
        }
        sb.append("</ul>");
        sb.append("</div></body></html>");
        mailService.sendHtml(organizerEmail, title + " - " + (eventTitle == null ? "Evenement" : eventTitle), sb.toString());
    }

    private String buildQrFileName(String ticketCode) {
        String normalized = ticketCode == null ? "ticket" : ticketCode.replaceAll("[^a-zA-Z0-9_-]", "");
        if (normalized.isBlank()) {
            normalized = "ticket";
        }
        return "qr-" + normalized + ".png";
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
