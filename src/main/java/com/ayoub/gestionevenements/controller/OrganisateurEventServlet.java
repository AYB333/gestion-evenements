package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.dao.OrganisateurDAO;
import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.Organisateur;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.EventService;
import com.ayoub.gestionevenements.service.TicketService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/organisateur/events")
@RolesAllowed({"ORGANISATEUR"})
public class OrganisateurEventServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "Concert", "Conference", "Spectacle", "Festival", "Theatre",
            "Cinema", "Sport", "Workshop", "Exposition", "Autre"
    );
    private static final Set<String> ALLOWED_CITIES = Set.of(
            "Casablanca", "Rabat", "Marrakech", "Fes", "Tanger",
            "Agadir", "Oujda", "Kenitra", "Tetouan", "Meknes"
    );

    @Inject
    private EventService eventService;

    @Inject
    private OrganisateurDAO organisateurDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private TicketService ticketService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }
        HttpSession session = req.getSession(false);

        Organisateur organisateur = organisateurDAO.findByUserId(user.getId());
        if (organisateur == null) {
            req.setAttribute("error", "Profil organisateur introuvable.");
            req.getRequestDispatcher("/organisateur-events.jsp").forward(req, resp);
            return;
        }

        String action = req.getParameter("action");
        if ("new".equalsIgnoreCase(action) || "edit".equalsIgnoreCase(action)) {
            Event event = null;
            if ("edit".equalsIgnoreCase(action)) {
                event = loadEventForOrganisateur(req, organisateur);
                if (event == null) {
                    resp.sendRedirect(req.getContextPath() + "/organisateur/events");
                    return;
                }
            }
            if (event == null) {
                event = new Event();
            }

            req.setAttribute("event", event);
            req.setAttribute("editMode", "edit".equalsIgnoreCase(action));
            req.setAttribute("dateDebutValue", toInputValue(event.getDateDebut()));
            req.setAttribute("dateFinValue", toInputValue(event.getDateFin()));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        List<Event> events = eventService.findByOrganisateur(organisateur.getId());
        Map<Long, Long> soldByEvent = new HashMap<>();
        Map<Long, Long> reservedByEvent = new HashMap<>();

        for (Event event : events) {
            Long eventId = event.getId();
            if (eventId == null) {
                continue;
            }
            soldByEvent.put(eventId, ticketService.countByEventAndStatus(eventId, com.ayoub.gestionevenements.model.Ticket.Status.PAYE));
            reservedByEvent.put(eventId, ticketService.countByEventAndStatus(eventId, com.ayoub.gestionevenements.model.Ticket.Status.RESERVE));
        }

        long totalEvents = events.size();
        long publishedCount = events.stream().filter(event -> event.getStatut() == Event.Status.PUBLIE).count();
        long pendingCount = events.stream().filter(event -> event.getStatut() == Event.Status.EN_ATTENTE).count();
        long cancelledCount = events.stream().filter(event -> event.getStatut() == Event.Status.ANNULE).count();

        long totalTickets = ticketService.countByOrganisateur(organisateur.getId());
        long paidTickets = ticketService.countByOrganisateurAndStatus(organisateur.getId(), com.ayoub.gestionevenements.model.Ticket.Status.PAYE);
        java.math.BigDecimal revenue = ticketService.sumRevenueByOrganisateur(organisateur.getId());

        req.setAttribute("events", events);
        req.setAttribute("totalEvents", totalEvents);
        req.setAttribute("publishedCount", publishedCount);
        req.setAttribute("pendingCount", pendingCount);
        req.setAttribute("cancelledCount", cancelledCount);
        req.setAttribute("totalTickets", totalTickets);
        req.setAttribute("paidTickets", paidTickets);
        req.setAttribute("revenue", revenue);
        req.setAttribute("soldByEvent", soldByEvent);
        req.setAttribute("reservedByEvent", reservedByEvent);
        req.setAttribute("dateDisplayByEvent", buildDateDisplayByEvent(events));
        req.setAttribute("success", consumeFlash(session, "flashSuccess"));
        req.setAttribute("error", consumeFlash(session, "flashError"));
        req.getRequestDispatcher("/organisateur-events.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        Organisateur organisateur = organisateurDAO.findByUserId(user.getId());
        if (organisateur == null) {
            resp.sendRedirect(req.getContextPath() + "/organisateur/events");
            return;
        }

        HttpSession session = req.getSession(false);
        String csrf = req.getParameter("csrfToken");
        String sessionToken = session == null ? null : (String) session.getAttribute("csrfToken");
        if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            Event event = loadEventForOrganisateur(req, organisateur);
            if (event != null && event.getStatut() != Event.Status.PUBLIE) {
                eventService.delete(event.getId());
                setFlash(session, "flashSuccess", "Evenement supprime.");
            } else {
                setFlash(session, "flashError", "Suppression impossible pour cet evenement.");
            }
            resp.sendRedirect(req.getContextPath() + "/organisateur/events");
            return;
        }

        Event event = loadEventForOrganisateur(req, organisateur);
        boolean editing = event != null;
        if (event == null) {
            event = new Event();
            event.setOrganisateur(organisateur);
        }

        String titre = req.getParameter("titre");
        String description = req.getParameter("description");
        String categorie = req.getParameter("categorie");
        String lieu = req.getParameter("lieu");
        LocalDateTime dateDebut = parseDateTime(req.getParameter("dateDebut"));
        LocalDateTime dateFin = parseDateTime(req.getParameter("dateFin"));
        BigDecimal prix = parseBigDecimal(req.getParameter("prix"));
        Integer capacite = parseInteger(req.getParameter("capacite"));

        if (isBlank(titre) || isBlank(categorie) || isBlank(lieu) || dateDebut == null || prix == null || capacite == null) {
            req.setAttribute("error", "Merci de remplir tous les champs obligatoires.");
            req.setAttribute("event", event);
            req.setAttribute("editMode", editing);
            req.setAttribute("dateDebutValue", toInputValue(dateDebut));
            req.setAttribute("dateFinValue", toInputValue(dateFin));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        if (titre.trim().length() > 200 || (description != null && description.trim().length() > 2000)) {
            req.setAttribute("error", "Titre ou description trop longs.");
            req.setAttribute("event", event);
            req.setAttribute("editMode", editing);
            req.setAttribute("dateDebutValue", toInputValue(dateDebut));
            req.setAttribute("dateFinValue", toInputValue(dateFin));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        String cleanCategorie = categorie.trim();
        String cleanLieu = lieu.trim();
        if (!ALLOWED_CATEGORIES.contains(cleanCategorie) || !ALLOWED_CITIES.contains(cleanLieu)) {
            req.setAttribute("error", "Categorie ou ville invalide.");
            req.setAttribute("event", event);
            req.setAttribute("editMode", editing);
            req.setAttribute("dateDebutValue", toInputValue(dateDebut));
            req.setAttribute("dateFinValue", toInputValue(dateFin));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        if (prix.compareTo(BigDecimal.ZERO) < 0 || capacite <= 0) {
            req.setAttribute("error", "Prix et capacite doivent etre positifs.");
            req.setAttribute("event", event);
            req.setAttribute("editMode", editing);
            req.setAttribute("dateDebutValue", toInputValue(dateDebut));
            req.setAttribute("dateFinValue", toInputValue(dateFin));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            req.setAttribute("error", "La date de fin doit etre apres la date de debut.");
            req.setAttribute("event", event);
            req.setAttribute("editMode", editing);
            req.setAttribute("dateDebutValue", toInputValue(dateDebut));
            req.setAttribute("dateFinValue", toInputValue(dateFin));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        if (dateDebut.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
            req.setAttribute("error", "La date de debut doit etre dans le futur.");
            req.setAttribute("event", event);
            req.setAttribute("editMode", editing);
            req.setAttribute("dateDebutValue", toInputValue(dateDebut));
            req.setAttribute("dateFinValue", toInputValue(dateFin));
            req.setAttribute("minDateTimeNow", toInputValue(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
            req.setAttribute("categories", new LinkedHashSet<>(ALLOWED_CATEGORIES));
            req.setAttribute("cities", new LinkedHashSet<>(ALLOWED_CITIES));
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        event.setTitre(titre.trim());
        event.setDescription(description == null ? null : description.trim());
        event.setCategorie(cleanCategorie);
        event.setLieu(cleanLieu);
        event.setDateDebut(dateDebut);
        event.setDateFin(dateFin);
        event.setPrix(prix);
        event.setCapacite(capacite);

        if (editing && event.getStatut() == Event.Status.PUBLIE) {
            event.setStatut(Event.Status.EN_ATTENTE);
        } else if (!editing && event.getStatut() == null) {
            event.setStatut(Event.Status.EN_ATTENTE);
        }

        if (editing) {
            eventService.update(event);
            setFlash(session, "flashSuccess", "Evenement mis a jour et renvoye en validation.");
        } else {
            eventService.create(event);
            setFlash(session, "flashSuccess", "Evenement cree avec succes.");
        }

        resp.sendRedirect(req.getContextPath() + "/organisateur/events");
    }

    private Event loadEventForOrganisateur(HttpServletRequest req, Organisateur organisateur) {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            return null;
        }
        try {
            Long id = Long.valueOf(idParam);
            Event event = eventService.findById(id);
            if (event == null || event.getOrganisateur() == null) {
                return null;
            }
            if (!event.getOrganisateur().getId().equals(organisateur.getId())) {
                return null;
            }
            return event;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private User ensureSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User existing = (User) session.getAttribute("user");
            if (existing != null) {
                return existing;
            }
        }

        if (req.getUserPrincipal() == null) {
            return null;
        }

        String email = req.getUserPrincipal().getName();
        User user = userDAO.findByEmail(email);
        if (user == null) {
            return null;
        }

        session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());
        }
        return user;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String toInputValue(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMAT);
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Map<Long, String> buildDateDisplayByEvent(List<Event> events) {
        Map<Long, String> display = new HashMap<>();
        for (Event event : events) {
            if (event.getId() == null) {
                continue;
            }
            display.put(event.getId(), event.getDateDebut() == null ? "-" : event.getDateDebut().format(DISPLAY_DATE_FORMAT));
        }
        return display;
    }

    private void setFlash(HttpSession session, String key, String message) {
        if (session != null) {
            session.setAttribute(key, message);
        }
    }

    private String consumeFlash(HttpSession session, String key) {
        if (session == null) {
            return null;
        }
        String value = (String) session.getAttribute(key);
        if (value != null) {
            session.removeAttribute(key);
        }
        return value;
    }
}
