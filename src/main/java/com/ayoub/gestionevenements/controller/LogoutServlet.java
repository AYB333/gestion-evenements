package com.ayoub.gestionevenements.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String csrf = req.getParameter("csrfToken");
            String sessionToken = (String) session.getAttribute("csrfToken");
            if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/auth");
    }
}
