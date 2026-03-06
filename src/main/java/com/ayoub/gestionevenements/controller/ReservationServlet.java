package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.EventService;
import com.ayoub.gestionevenements.service.TicketService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/reserver")
public class ReservationServlet extends HttpServlet {

    @Inject
    private TicketService ticketService;

    @Inject
    private EventService eventService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        String csrf = req.getParameter("csrfToken");
        String sessionToken = (String) session.getAttribute("csrfToken");
        if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
            forwardWithError(req, resp, "Action non autorisee.");
            return;
        }

        String eventIdParam = req.getParameter("eventId");
        Long eventId = null;
        try {
            eventId = Long.valueOf(eventIdParam);
        } catch (NumberFormatException ex) {
            forwardWithError(req, resp, "Evenement invalide.");
            return;
        }

        User user = (User) session.getAttribute("user");
        Ticket ticket = ticketService.reserverTicket(eventId, user.getId());
        if (ticket == null) {
            forwardWithError(req, resp, "Evenement complet ou indisponible.");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/mes-billets");
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String message) throws ServletException, IOException {
        req.setAttribute("error", message);
        req.setAttribute("events", eventService.trouverEvenementsPublies());
        req.getRequestDispatcher("/events.jsp").forward(req, resp);
    }
}
