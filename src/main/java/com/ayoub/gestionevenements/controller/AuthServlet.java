package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.AuthService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    @Inject
    private AuthService authService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("register".equalsIgnoreCase(action)) {
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        if ("register".equalsIgnoreCase(action)) {
            handleRegister(req, resp);
        } else {
            handleLogin(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String roleParam = req.getParameter("role");

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            req.setAttribute("error", "Veuillez remplir tous les champs obligatoires.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(password);
        user.setRole(resolveRole(roleParam));

        User created = authService.register(user);
        if (created == null) {
            req.setAttribute("error", "Email deja utilise ou donnees invalides.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        setSessionUser(session, created);

        resp.sendRedirect(req.getContextPath() + "/events");
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Identifiants invalides.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = authService.login(email, password);
        if (user == null) {
            req.setAttribute("error", "Email ou mot de passe incorrect.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        setSessionUser(session, user);

        resp.sendRedirect(req.getContextPath() + "/events");
    }

    private void setSessionUser(HttpSession session, User user) {
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
    }

    private User.Role resolveRole(String roleParam) {
        if (roleParam == null) {
            return User.Role.PARTICIPANT;
        }
        if ("ORGANISATEUR".equalsIgnoreCase(roleParam)) {
            return User.Role.ORGANISATEUR;
        }
        return User.Role.PARTICIPANT;
    }
}
