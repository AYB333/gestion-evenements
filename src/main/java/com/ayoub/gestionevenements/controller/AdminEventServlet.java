package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.dao.UserDAO;
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
import java.util.List;

@WebServlet("/admin/events")
@RolesAllowed({"ADMIN"})
public class AdminEventServlet extends HttpServlet {

    @Inject
    private EventService eventService;

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ensureSessionUser(req);
        List<Event> pending = eventService.findByStatus(Event.Status.EN_ATTENTE);
        req.setAttribute("events", pending);
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
            resp.sendRedirect(req.getContextPath() + "/admin/events");
            return;
        }

        try {
            Long id = Long.valueOf(idParam);
            Event event = eventService.findById(id);
            if (event == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/events");
                return;
            }

            if ("approve".equalsIgnoreCase(action)) {
                event.setStatut(Event.Status.PUBLIE);
                eventService.update(event);
            } else if ("reject".equalsIgnoreCase(action)) {
                event.setStatut(Event.Status.ANNULE);
                eventService.update(event);
            }
        } catch (NumberFormatException ignored) {
            // ignore invalid ids
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
}
