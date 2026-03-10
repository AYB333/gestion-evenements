package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CsvReportService {

    private static final char SEPARATOR = ';';
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] buildAdminEventsReport(List<Event> events,
                                         Map<Long, Long> reservedByEvent,
                                         Map<Long, Long> soldByEvent,
                                         Map<Long, BigDecimal> revenueByEvent) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, "Rapport", "Administration - Evenements");
        appendRow(csv, "GenereLe", formatDate(LocalDateTime.now()));
        csv.append('\n');
        appendRow(csv,
                "EvenementId",
                "Titre",
                "Organisateur",
                "Categorie",
                "Lieu",
                "DateDebut",
                "Statut",
                "CapaciteRestante",
                "BilletsReserves",
                "BilletsPayes",
                "RevenusMAD");

        for (Event event : events) {
            Long eventId = event.getId();
            appendRow(csv,
                    eventId == null ? "" : String.valueOf(eventId),
                    event.getTitre(),
                    event.getOrganisateur() != null && event.getOrganisateur().getUser() != null
                            ? event.getOrganisateur().getUser().getFullName()
                            : "",
                    event.getCategorie(),
                    event.getLieu(),
                    formatDate(event.getDateDebut()),
                    event.getStatut() == null ? "" : event.getStatut().name(),
                    event.getCapacite() == null ? "" : String.valueOf(event.getCapacite()),
                    String.valueOf(valueForLong(eventId, reservedByEvent)),
                    String.valueOf(valueForLong(eventId, soldByEvent)),
                    formatMoney(valueForMoney(eventId, revenueByEvent)));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] buildOrganizerEventsReport(List<Event> events,
                                             Map<Long, Long> reservedByEvent,
                                             Map<Long, Long> soldByEvent,
                                             Map<Long, BigDecimal> revenueByEvent) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, "Rapport", "Organisateur - Mes evenements");
        appendRow(csv, "GenereLe", formatDate(LocalDateTime.now()));
        csv.append('\n');
        appendRow(csv,
                "EvenementId",
                "Titre",
                "Categorie",
                "Lieu",
                "DateDebut",
                "Statut",
                "CapaciteRestante",
                "BilletsReserves",
                "BilletsPayes",
                "RevenusMAD");

        for (Event event : events) {
            Long eventId = event.getId();
            appendRow(csv,
                    eventId == null ? "" : String.valueOf(eventId),
                    event.getTitre(),
                    event.getCategorie(),
                    event.getLieu(),
                    formatDate(event.getDateDebut()),
                    event.getStatut() == null ? "" : event.getStatut().name(),
                    event.getCapacite() == null ? "" : String.valueOf(event.getCapacite()),
                    String.valueOf(valueForLong(eventId, reservedByEvent)),
                    String.valueOf(valueForLong(eventId, soldByEvent)),
                    formatMoney(valueForMoney(eventId, revenueByEvent)));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] buildUsersReport(List<User> users) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, "Rapport", "Administration - Utilisateurs");
        appendRow(csv, "GenereLe", formatDate(LocalDateTime.now()));
        csv.append('\n');
        appendRow(csv, "UtilisateurId", "Nom", "Email", "Role", "Statut", "DateCreation");

        for (User user : users) {
            appendRow(csv,
                    user.getId() == null ? "" : String.valueOf(user.getId()),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole() == null ? "" : user.getRole().name(),
                    user.isEnabled() ? "ACTIF" : "BLOQUE",
                    formatDate(user.getCreatedAt()));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(SEPARATOR);
            }
            csv.append(escape(values[i]));
        }
        csv.append('\n');
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.indexOf(SEPARATOR) >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private long valueForLong(Long eventId, Map<Long, Long> source) {
        if (eventId == null || source == null) {
            return 0;
        }
        Long value = source.get(eventId);
        return value == null ? 0 : value;
    }

    private BigDecimal valueForMoney(Long eventId, Map<Long, BigDecimal> source) {
        if (eventId == null || source == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = source.get(eventId);
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMAT);
    }

    private String formatMoney(BigDecimal value) {
        return value == null ? "0.00" : value.toPlainString();
    }
}
