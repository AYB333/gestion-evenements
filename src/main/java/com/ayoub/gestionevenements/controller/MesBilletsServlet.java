package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.service.TicketService;
import com.ayoub.gestionevenements.service.QrCodeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/mes-billets")
@RolesAllowed({"PARTICIPANT"})
public class MesBilletsServlet extends HttpServlet {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private TicketService ticketService;

    @Inject
    private QrCodeService qrCodeService;

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
        if (session != null && session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
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

        Map<Long, String> qrCodes = new HashMap<>();
        Map<Long, String> eventDateDisplayByTicket = new HashMap<>();
        Map<Long, String> createdAtDisplayByTicket = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getId() != null) {
                LocalDateTime eventDate = ticket.getEvent() == null ? null : ticket.getEvent().getDateDebut();
                LocalDateTime createdAt = ticket.getCreatedAt();
                eventDateDisplayByTicket.put(ticket.getId(), eventDate == null ? "-" : eventDate.format(DISPLAY_DATE_FORMAT));
                createdAtDisplayByTicket.put(ticket.getId(), createdAt == null ? "-" : createdAt.format(DISPLAY_DATE_FORMAT));
            }
            if (ticket.getStatut() == Ticket.Status.PAYE) {
                String qr = qrCodeService.generateBase64("TICKET:" + ticket.getCode(), 180);
                if (qr != null) {
                    qrCodes.put(ticket.getId(), qr);
                }
            }
        }

        req.setAttribute("tickets", tickets);
        req.setAttribute("qrCodes", qrCodes);
        req.setAttribute("eventDateDisplayByTicket", eventDateDisplayByTicket);
        req.setAttribute("createdAtDisplayByTicket", createdAtDisplayByTicket);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("statusFilter", statusFilter == null ? "ALL" : statusFilter.name());
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("success", consumeFlash(session, "flashSuccess"));
        req.setAttribute("error", consumeFlash(session, "flashError"));
        req.getRequestDispatcher("/mes-billets.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        HttpSession session = req.getSession(false);
        String csrf = req.getParameter("csrfToken");
        String sessionToken = session == null ? null : (String) session.getAttribute("csrfToken");
        if (sessionToken == null || csrf == null || !sessionToken.equals(csrf)) {
            if (session != null) {
                setFlash(session, "flashError", "Action non autorisee.");
            }
            resp.sendRedirect(req.getContextPath() + "/mes-billets");
            return;
        }

        String action = req.getParameter("action");
        Long ticketId = parseLong(req.getParameter("ticketId"));
        if (ticketId == null || action == null || action.isBlank()) {
            setFlash(session, "flashError", "Ticket invalide.");
            resp.sendRedirect(req.getContextPath() + "/mes-billets");
            return;
        }

        if ("cancel".equalsIgnoreCase(action)) {
            boolean ok = ticketService.annulerTicket(ticketId, user.getId());
            setFlash(session, ok ? "flashSuccess" : "flashError",
                    ok ? "Billet annule avec succes." : "Annulation impossible.");
            resp.sendRedirect(req.getContextPath() + "/mes-billets");
            return;
        }

        if ("transfer".equalsIgnoreCase(action)) {
            String targetEmail = req.getParameter("targetEmail");
            TicketService.TransferResult result = ticketService.transfererTicketAvecMessage(ticketId, user.getId(), targetEmail);
            setFlash(session, result.success() ? "flashSuccess" : "flashError", result.message());
            resp.sendRedirect(req.getContextPath() + "/mes-billets");
            return;
        }

        setFlash(session, "flashError", "Action non supportee.");
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
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
        return user;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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
