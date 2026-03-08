package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.User;
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

@WebServlet("/admin/users")
@RolesAllowed({"ADMIN"})
public class AdminUserServlet extends HttpServlet {

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = ensureSessionUser(req);
        HttpSession session = req.getSession(false);
        List<User> users = userDAO.findAll();
        req.setAttribute("users", users);
        req.setAttribute("currentUserId", currentUser == null ? null : currentUser.getId());
        req.setAttribute("success", consumeFlash(session, "flashSuccess"));
        req.setAttribute("error", consumeFlash(session, "flashError"));
        req.getRequestDispatcher("/admin-users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = ensureSessionUser(req);

        HttpSession session = req.getSession(false);
        String csrf = req.getParameter("csrfToken");
        String sessionToken = session == null ? null : (String) session.getAttribute("csrfToken");
        if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                Long id = Long.valueOf(idParam);
                User user = userDAO.findById(id);
                if (user == null) {
                    setFlash(session, "flashError", "Utilisateur introuvable.");
                } else if (user.getRole() == User.Role.ADMIN
                        || (currentUser != null && user.getId().equals(currentUser.getId()))) {
                    setFlash(session, "flashError", "Ce compte est protege.");
                } else {
                    user.setEnabled(!user.isEnabled());
                    userDAO.update(user);
                    setFlash(session, "flashSuccess", user.isEnabled() ? "Compte active." : "Compte desactive.");
                }
            } catch (NumberFormatException ignored) {
                setFlash(session, "flashError", "Identifiant utilisateur invalide.");
            }
        } else {
            setFlash(session, "flashError", "Action utilisateur invalide.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private User ensureSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return (User) session.getAttribute("user");
        }
        if (req.getUserPrincipal() == null) {
            return null;
        }
        User user = userDAO.findByEmail(req.getUserPrincipal().getName());
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
