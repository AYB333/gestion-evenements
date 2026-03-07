package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.dao.OrganisateurDAO;
import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.Organisateur;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.EventService;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/organisateur/events")
@RolesAllowed({"ORGANISATEUR"})
public class OrganisateurEventServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Inject
    private EventService eventService;

    @Inject
    private OrganisateurDAO organisateurDAO;

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

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
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        List<Event> events = eventService.findByOrganisateur(organisateur.getId());
        req.setAttribute("events", events);
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
            req.getRequestDispatcher("/organisateur-event-form.jsp").forward(req, resp);
            return;
        }

        event.setTitre(titre.trim());
        event.setDescription(description == null ? null : description.trim());
        event.setCategorie(categorie.trim());
        event.setLieu(lieu.trim());
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
        } else {
            eventService.create(event);
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
}
