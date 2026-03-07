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
import java.util.List;

@WebServlet("/mes-billets")
@RolesAllowed({"PARTICIPANT"})
public class MesBilletsServlet extends HttpServlet {

    @Inject
    private TicketService ticketService;

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        String statusParam = req.getParameter("status");
        Ticket.Status statusFilter = null;
        if (statusParam != null && !statusParam.isBlank() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                statusFilter = Ticket.Status.valueOf(statusParam.toUpperCase());
            } catch (IllegalArgumentException ex) {
                statusFilter = null;
            }
        }

        int page = 1;
        try {
            page = Integer.parseInt(req.getParameter("page"));
        } catch (NumberFormatException ignored) {
            page = 1;
        }
        int pageSize = 6;

        long totalCount = ticketService.countTicketsParParticipant(user.getId(), statusFilter);
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        List<Ticket> tickets = ticketService.trouverTicketsParParticipant(user.getId(), statusFilter, page, pageSize);

        req.setAttribute("tickets", tickets);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("statusFilter", statusFilter == null ? "ALL" : statusFilter.name());
        req.setAttribute("totalCount", totalCount);
        req.getRequestDispatcher("/mes-billets.jsp").forward(req, resp);
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
