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
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet("/events")
@RolesAllowed({"PARTICIPANT", "ORGANISATEUR", "ADMIN"})
public class EventServlet extends HttpServlet {

    private static final DateTimeFormatter CARD_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> ALLOWED_CATEGORIES = List.of(
            "Concert", "Conference", "Spectacle", "Festival", "Theatre",
            "Cinema", "Sport", "Workshop", "Exposition", "Autre"
    );
    private static final List<String> ALLOWED_CITIES = List.of(
            "Casablanca", "Rabat", "Marrakech", "Fes", "Tanger",
            "Agadir", "Oujda", "Kenitra", "Tetouan", "Meknes"
    );

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
        Object flashError = session.getAttribute("flashError");
        if (flashError != null) {
            req.setAttribute("error", flashError.toString());
            session.removeAttribute("flashError");
        }

        List<Event> allEvents = eventService.trouverEvenementsPublies();

        String query = req.getParameter("q");

        String categoryParam = req.getParameter("category");
        String normalizedCategory = categoryParam;
        if (normalizedCategory != null && normalizedCategory.isBlank()) {
            normalizedCategory = null;
        }
        if ("ALL".equalsIgnoreCase(normalizedCategory)) {
            normalizedCategory = null;
        }
        if (normalizedCategory != null && !containsIgnoreCase(ALLOWED_CATEGORIES, normalizedCategory)) {
            normalizedCategory = null;
        }

        String cityParam = req.getParameter("city");
        String normalizedCity = cityParam;
        if (normalizedCity != null && normalizedCity.isBlank()) {
            normalizedCity = null;
        }
        if ("ALL".equalsIgnoreCase(normalizedCity)) {
            normalizedCity = null;
        }
        if (normalizedCity != null && !containsIgnoreCase(ALLOWED_CITIES, normalizedCity)) {
            normalizedCity = null;
        }

        final String category = normalizedCategory;
        final String city = normalizedCity;
        final String qLower = query == null ? null : query.trim().toLowerCase(Locale.ROOT);

        String dateParam = req.getParameter("date");
        LocalDate filterDate = null;
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                filterDate = LocalDate.parse(dateParam);
            } catch (DateTimeParseException ignored) {
                filterDate = null;
            }
        }
        final LocalDate selectedDate = filterDate;

        List<Event> filtered = allEvents.stream()
                .filter(event -> {
                    if (category != null && event.getCategorie() != null) {
                        return category.equalsIgnoreCase(event.getCategorie().trim());
                    }
                    return category == null;
                })
                .filter(event -> {
                    if (city != null && event.getLieu() != null) {
                        return city.equalsIgnoreCase(event.getLieu().trim());
                    }
                    return city == null;
                })
                .filter(event -> {
                    if (qLower == null || qLower.isBlank()) {
                        return true;
                    }
                    String title = event.getTitre() == null ? "" : event.getTitre().toLowerCase(Locale.ROOT);
                    String desc = event.getDescription() == null ? "" : event.getDescription().toLowerCase(Locale.ROOT);
                    return title.contains(qLower) || desc.contains(qLower);
                })
                .filter(event -> selectedDate == null
                        || (event.getDateDebut() != null && event.getDateDebut().toLocalDate().equals(selectedDate)))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        Map<Long, String> dateDisplayByEvent = new HashMap<>();
        Map<Long, String> reservationStateByEvent = new HashMap<>();
        Map<Long, String> categoryDisplayByEvent = new HashMap<>();

        for (Event event : filtered) {
            if (event.getId() == null) {
                continue;
            }
            dateDisplayByEvent.put(
                    event.getId(),
                    event.getDateDebut() == null ? "-" : event.getDateDebut().format(CARD_DATE_FORMAT)
            );

            String reservationState = "OK";
            if (event.getCapacite() == null || event.getCapacite() <= 0) {
                reservationState = "COMPLET";
            } else if (event.getDateDebut() != null && event.getDateDebut().isBefore(now)) {
                reservationState = "PASSE";
            }
            reservationStateByEvent.put(event.getId(), reservationState);
            categoryDisplayByEvent.put(event.getId(), normalizeCategoryLabel(event.getCategorie()));
        }

        List<String> categories = ALLOWED_CATEGORIES;
        List<String> cities = ALLOWED_CITIES;

        filtered.sort(
                Comparator
                        .comparing((Event event) -> event.getDateDebut() != null && event.getDateDebut().isBefore(now))
                        .thenComparing(Event::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        req.setAttribute("events", filtered);
        req.setAttribute("categories", categories);
        req.setAttribute("cities", cities);
        req.setAttribute("filterQ", query);
        req.setAttribute("filterCategory", category == null ? "ALL" : category);
        req.setAttribute("filterCity", city == null ? "ALL" : city);
        req.setAttribute("filterDate", dateParam == null ? "" : dateParam);
        req.setAttribute("dateDisplayByEvent", dateDisplayByEvent);
        req.setAttribute("reservationStateByEvent", reservationStateByEvent);
        req.setAttribute("categoryDisplayByEvent", categoryDisplayByEvent);
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

    private String normalizeCategoryLabel(String value) {
        if (value == null || value.isBlank()) {
            return "Autre";
        }
        return ALLOWED_CATEGORIES.stream()
                .filter(label -> label.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse("Autre");
    }

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        if (candidate == null) {
            return false;
        }
        return values.stream().anyMatch(value -> value.equalsIgnoreCase(candidate));
    }
}
