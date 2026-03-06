package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.TicketService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/mes-billets")
public class MesBilletsServlet extends HttpServlet {

    @Inject
    private TicketService ticketService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        User user = (User) session.getAttribute("user");
        List<Ticket> tickets = ticketService.trouverTicketsParParticipant(user.getId());

        req.setAttribute("tickets", tickets);
        req.getRequestDispatcher("/mes-billets.jsp").forward(req, resp);
    }
}
