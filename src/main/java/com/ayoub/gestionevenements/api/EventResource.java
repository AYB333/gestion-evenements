package com.ayoub.gestionevenements.api;

import com.ayoub.gestionevenements.model.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {
    // Read-only REST endpoint exposing published events outside the JSP UI.

    @GET
    public Response list(@QueryParam("q") String q,
                         @QueryParam("category") String category,
                         @QueryParam("city") String city,
                         @QueryParam("date") String date) {
        try (EntityManager em = ApiPersistence.createEntityManager()) {
            StringBuilder jpql = new StringBuilder(
                    "select e from Event e where e.statut = :published ");

            String query = normalize(q);
            String normalizedCategory = normalize(category);
            String normalizedCity = normalize(city);
            LocalDate parsedDate = parseDate(date);

            if (query != null) {
                jpql.append("and (lower(e.titre) like :q or lower(e.description) like :q) ");
            }
            if (normalizedCategory != null) {
                jpql.append("and lower(e.categorie) = :category ");
            }
            if (normalizedCity != null) {
                jpql.append("and lower(e.lieu) = :city ");
            }
            if (parsedDate != null) {
                jpql.append("and e.dateDebut >= :startDate and e.dateDebut < :endDate ");
            }
            jpql.append("order by e.dateDebut asc");

            TypedQuery<Event> queryObj = em.createQuery(jpql.toString(), Event.class)
                    .setParameter("published", Event.Status.PUBLIE);

            if (query != null) {
                queryObj.setParameter("q", "%" + query + "%");
            }
            if (normalizedCategory != null) {
                queryObj.setParameter("category", normalizedCategory);
            }
            if (normalizedCity != null) {
                queryObj.setParameter("city", normalizedCity);
            }
            if (parsedDate != null) {
                queryObj.setParameter("startDate", parsedDate.atStartOfDay());
                queryObj.setParameter("endDate", parsedDate.plusDays(1).atStartOfDay());
            }

            List<EventItem> payload = new ArrayList<>();
            for (Event event : queryObj.getResultList()) {
                payload.add(toItem(event));
            }
            return Response.ok(payload).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError("Invalid event id"))
                    .build();
        }
        try (EntityManager em = ApiPersistence.createEntityManager()) {
            Event event = em.find(Event.class, id);
            if (event == null || event.getStatut() != Event.Status.PUBLIE) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ApiError("Event not found"))
                        .build();
            }
            return Response.ok(toItem(event)).build();
        }
    }

    private EventItem toItem(Event event) {
        return new EventItem(
                event.getId(),
                event.getTitre(),
                event.getDescription(),
                event.getCategorie(),
                event.getLieu(),
                event.getDateDebut() == null ? null : event.getDateDebut().toString(),
                event.getDateFin() == null ? null : event.getDateFin().toString(),
                event.getPrix(),
                event.getCapacite(),
                event.getStatut() == null ? null : event.getStatut().name()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "all".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public record EventItem(Long id,
                            String titre,
                            String description,
                            String categorie,
                            String lieu,
                            String dateDebut,
                            String dateFin,
                            java.math.BigDecimal prix,
                            Integer capacite,
                            String statut) {
    }

    public record ApiError(String message) {
    }
}

