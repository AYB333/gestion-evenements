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
import java.util.UUID;

@WebServlet("/events")
@RolesAllowed({"PARTICIPANT", "ORGANISATEUR", "ADMIN"})
public class EventServlet extends HttpServlet {

    @Inject
    private EventService eventService;

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        List<Event> events = eventService.trouverEvenementsPublies();
        req.setAttribute("events", events);
        req.getRequestDispatcher("/events.jsp").forward(req, resp);
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
        return user;
    }
}
