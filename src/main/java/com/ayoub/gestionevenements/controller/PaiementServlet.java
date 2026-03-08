package com.ayoub.gestionevenements.controller;

import com.ayoub.gestionevenements.dao.UserDAO;
import com.ayoub.gestionevenements.model.Paiement;
import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.service.PaiementService;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet("/paiement")
@RolesAllowed({"PARTICIPANT"})
public class PaiementServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^\\d{3,4}$");
    private static final DateTimeFormatter CARD_EXPIRY_FORMAT = DateTimeFormatter.ofPattern("MM/yy");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private TicketService ticketService;

    @Inject
    private PaiementService paiementService;

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ensureSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }

        Long ticketId = parseTicketId(req.getParameter("ticketId"));
        if (ticketId == null) {
            resp.sendRedirect(req.getContextPath() + "/mes-billets");
            return;
        }

        Ticket ticket = ticketService.findByIdWithEvent(ticketId);
        if (ticket == null || ticket.getParticipant() == null || !user.getId().equals(ticket.getParticipant().getId())) {
            resp.sendRedirect(req.getContextPath() + "/mes-billets");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        Paiement existing = paiementService.findByTicketId(ticketId);
        req.setAttribute("ticket", ticket);
        req.setAttribute("paiement", existing);
        req.setAttribute("eventDateDisplay", buildEventDateDisplay(ticket));
        req.getRequestDispatcher("/paiement.jsp").forward(req, resp);
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
            forwardWithError(req, resp, "Action non autorisee.");
            return;
        }

        Long ticketId = parseTicketId(req.getParameter("ticketId"));
        if (ticketId == null) {
            forwardWithError(req, resp, "Ticket invalide.");
            return;
        }

        Paiement.Methode methode = parseMethode(req.getParameter("methode"));
        if (methode == null) {
            forwardWithError(req, resp, "Methode de paiement invalide.");
            return;
        }
        String validationError = validatePaymentDetails(req, methode);
        if (validationError != null) {
            forwardWithError(req, resp, validationError);
            return;
        }

        Paiement paiement = paiementService.payer(ticketId, user.getId(), methode);
        if (paiement == null) {
            forwardWithError(req, resp, "Paiement impossible. Ticket deja paye ou invalide.");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/mes-billets");
    }

    private Long parseTicketId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Paiement.Methode parseMethode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Paiement.Methode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String validatePaymentDetails(HttpServletRequest req, Paiement.Methode methode) {
        return switch (methode) {
            case CARTE -> validateCard(
                    req.getParameter("cardHolder"),
                    req.getParameter("cardNumber"),
                    req.getParameter("cardExpiry"),
                    req.getParameter("cardCvv"));
            case PAYPAL -> validatePaypal(req.getParameter("paypalEmail"));
            case STRIPE -> validateStripe(
                    req.getParameter("stripeBillingName"),
                    req.getParameter("stripeEmail"));
        };
    }

    private String validateCard(String holder, String number, String expiry, String cvv) {
        if (holder == null || holder.isBlank() || holder.trim().length() < 3) {
            return "Nom du titulaire invalide.";
        }
        String cleanNumber = number == null ? "" : number.replaceAll("\\s+", "");
        if (!CARD_NUMBER_PATTERN.matcher(cleanNumber).matches()) {
            return "Numero de carte invalide.";
        }
        if (expiry == null || expiry.isBlank()) {
            return "Date d'expiration invalide.";
        }
        try {
            YearMonth expiryMonth = YearMonth.parse(expiry.trim(), CARD_EXPIRY_FORMAT);
            if (expiryMonth.isBefore(YearMonth.now())) {
                return "Carte expiree.";
            }
        } catch (DateTimeParseException ex) {
            return "Date d'expiration invalide.";
        }
        if (!CVV_PATTERN.matcher(cvv == null ? "" : cvv.trim()).matches()) {
            return "Code CVV invalide.";
        }
        return null;
    }

    private String validatePaypal(String paypalEmail) {
        if (paypalEmail == null || paypalEmail.isBlank()) {
            return "Email PayPal invalide.";
        }
        String normalized = paypalEmail.trim().toLowerCase(Locale.ROOT);
        return EMAIL_PATTERN.matcher(normalized).matches() ? null : "Email PayPal invalide.";
    }

    private String validateStripe(String billingName, String stripeEmail) {
        if (billingName == null || billingName.isBlank() || billingName.trim().length() < 3) {
            return "Nom de facturation Stripe invalide.";
        }
        if (stripeEmail == null || stripeEmail.isBlank()) {
            return "Email Stripe invalide.";
        }
        String normalized = stripeEmail.trim().toLowerCase(Locale.ROOT);
        return EMAIL_PATTERN.matcher(normalized).matches() ? null : "Email Stripe invalide.";
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

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String message) throws ServletException, IOException {
        req.setAttribute("error", message);
        Ticket ticket = ticketService.findByIdWithEvent(parseTicketId(req.getParameter("ticketId")));
        req.setAttribute("ticket", ticket);
        if (ticket != null) {
            req.setAttribute("paiement", paiementService.findByTicketId(ticket.getId()));
        }
        req.setAttribute("eventDateDisplay", buildEventDateDisplay(ticket));
        req.setAttribute("selectedMethod", req.getParameter("methode"));
        req.getRequestDispatcher("/paiement.jsp").forward(req, resp);
    }

    private String buildEventDateDisplay(Ticket ticket) {
        if (ticket == null || ticket.getEvent() == null || ticket.getEvent().getDateDebut() == null) {
            return "-";
        }
        return ticket.getEvent().getDateDebut().format(DISPLAY_DATE_FORMAT);
    }
}
