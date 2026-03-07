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
        ensureSessionUser(req);
        List<User> users = userDAO.findAll();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/admin-users.jsp").forward(req, resp);
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

        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                Long id = Long.valueOf(idParam);
                User user = userDAO.findById(id);
                if (user != null) {
                    user.setEnabled(!user.isEnabled());
                    userDAO.update(user);
                }
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
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
