package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.dao.UserDAO;
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

@WebServlet("/reserver")
@RolesAllowed({"PARTICIPANT"})
public class ReservationServlet extends HttpServlet {

    @Inject
    private TicketService ticketService;

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        // Only participants can book seats. Organizers and admins only manage data.
        if (user.getRole() != User.Role.PARTICIPANT) {
            redirectWithError(req, resp, "Seuls les participants peuvent reserver.");
            return;
        }

        HttpSession session = req.getSession(false);

        String csrf = req.getParameter("csrfToken");
        String sessionToken = (String) session.getAttribute("csrfToken");
        if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
            redirectWithError(req, resp, "Action non autorisee.");
            return;
        }

        String eventIdParam = req.getParameter("eventId");
        Long eventId = null;
        try {
            eventId = Long.valueOf(eventIdParam);
        } catch (NumberFormatException ex) {
            redirectWithError(req, resp, "Evenement invalide.");
            return;
        }

        if (ticketService.dejaReserve(eventId, user.getId())) {
            redirectWithError(req, resp, "Reservation deja faite pour cet evenement.");
            return;
        }

        Ticket ticket = ticketService.reserverTicket(eventId, user.getId());
        if (ticket == null) {
            redirectWithError(req, resp, "Evenement complet, passe ou indisponible.");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/mes-billets");
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

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        HttpSession session = req.getSession(true);
        session.setAttribute("flashError", message);
        resp.sendRedirect(req.getContextPath() + "/events");
    }
}
