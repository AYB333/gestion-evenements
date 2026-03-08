package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.dao.UserDAO;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/events")
@RolesAllowed({"ADMIN"})
public class AdminEventServlet extends HttpServlet {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private EventService eventService;

    @Inject
    private UserDAO userDAO;

    @Inject
    private TicketService ticketService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ensureSessionUser(req);
        List<Event> pending = eventService.findByStatus(Event.Status.EN_ATTENTE);
        List<Event> published = eventService.findByStatus(Event.Status.PUBLIE);
        List<Event> allEvents = eventService.findAll();
        HttpSession session = req.getSession(false);

        List<User> users = userDAO.findAll();
        long totalUsers = users.size();
        long participantCount = users.stream().filter(u -> u.getRole() == User.Role.PARTICIPANT).count();
        long organisateurCount = users.stream().filter(u -> u.getRole() == User.Role.ORGANISATEUR).count();

        long totalTickets = ticketService.countAll();
        long paidTickets = ticketService.countByStatus(com.ayoub.gestionevenements.model.Ticket.Status.PAYE);
        List<com.ayoub.gestionevenements.dao.TicketDAO.EventSalesRow> topEvents = ticketService.findTopEventsByPaidTickets(5);

        req.setAttribute("events", pending);
        req.setAttribute("totalEvents", allEvents.size());
        req.setAttribute("pendingCount", pending.size());
        req.setAttribute("publishedCount", published.size());
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("participantCount", participantCount);
        req.setAttribute("organisateurCount", organisateurCount);
        req.setAttribute("totalTickets", totalTickets);
        req.setAttribute("paidTickets", paidTickets);
        req.setAttribute("topEvents", topEvents);
        req.setAttribute("dateDisplayByEvent", buildDateDisplayByEvent(pending));
        req.setAttribute("success", consumeFlash(session, "flashSuccess"));
        req.setAttribute("error", consumeFlash(session, "flashError"));
        req.getRequestDispatcher("/admin-events.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ensureSessionUser(req);

        HttpSession session = req.getSession(false);
        String csrf = req.getParameter("csrfToken");
        String sessionToken = session == null ? null : (String) session.getAttribute("csrfToken");
        if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        String idParam = req.getParameter("id");
        if (action == null || idParam == null) {
            setFlash(session, "flashError", "Action admin invalide.");
            resp.sendRedirect(req.getContextPath() + "/admin/events");
            return;
        }

        try {
            Long id = Long.valueOf(idParam);
            Event event = eventService.findById(id);
            if (event == null) {
                setFlash(session, "flashError", "Evenement introuvable.");
                resp.sendRedirect(req.getContextPath() + "/admin/events");
                return;
            }

            if (event.getStatut() != Event.Status.EN_ATTENTE) {
                setFlash(session, "flashError", "Seuls les evenements en attente peuvent etre valides.");
            } else if ("approve".equalsIgnoreCase(action)) {
                event.setStatut(Event.Status.PUBLIE);
                eventService.update(event);
                setFlash(session, "flashSuccess", "Evenement approuve.");
            } else if ("reject".equalsIgnoreCase(action)) {
                event.setStatut(Event.Status.ANNULE);
                eventService.update(event);
                setFlash(session, "flashSuccess", "Evenement refuse.");
            } else {
                setFlash(session, "flashError", "Action admin invalide.");
            }
        } catch (NumberFormatException ignored) {
            setFlash(session, "flashError", "Identifiant evenement invalide.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/events");
    }

    private void ensureSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return;
        }
        if (req.getUserPrincipal() == null) {
            return;
        }
        User user = userDAO.findByEmail(req.getUserPrincipal().getName());
        if (user == null) {
            return;
        }
        session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());
        }
    }

    private Map<Long, String> buildDateDisplayByEvent(List<Event> events) {
        Map<Long, String> display = new HashMap<>();
        for (Event event : events) {
            if (event.getId() == null) {
                continue;
            }
            LocalDateTime dateDebut = event.getDateDebut();
            display.put(event.getId(), dateDebut == null ? "-" : dateDebut.format(DISPLAY_DATE_FORMAT));
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
