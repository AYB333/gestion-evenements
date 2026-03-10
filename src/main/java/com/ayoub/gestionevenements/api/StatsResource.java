package com.ayoub.gestionevenements.api;

import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {
    // Small REST summary used for demos and quick platform checks.

    @GET
    public Response overview() {
        try (EntityManager em = ApiPersistence.createEntityManager()) {
            long users = queryCount(em, "select count(u) from User u");
            long participants = queryCount(em, "select count(u) from User u where u.role = :role", "role", User.Role.PARTICIPANT);
            long organisateurs = queryCount(em, "select count(u) from User u where u.role = :role", "role", User.Role.ORGANISATEUR);
            long eventsTotal = queryCount(em, "select count(e) from Event e");
            long eventsPending = queryCount(em, "select count(e) from Event e where e.statut = :statut", "statut", Event.Status.EN_ATTENTE);
            long eventsPublished = queryCount(em, "select count(e) from Event e where e.statut = :statut", "statut", Event.Status.PUBLIE);
            long ticketsTotal = queryCount(em, "select count(t) from Ticket t");
            long ticketsPaid = queryCount(em, "select count(t) from Ticket t where t.statut = :statut", "statut", Ticket.Status.PAYE);

            StatsOverview payload = new StatsOverview(
                    users,
                    participants,
                    organisateurs,
                    eventsTotal,
                    eventsPending,
                    eventsPublished,
                    ticketsTotal,
                    ticketsPaid
            );
            return Response.ok(payload).build();
        }
    }

    private long queryCount(EntityManager em, String jpql) {
        Long count = em.createQuery(jpql, Long.class).getSingleResult();
        return count == null ? 0L : count;
    }

    private long queryCount(EntityManager em, String jpql, String parameterName, Object parameterValue) {
        Long count = em.createQuery(jpql, Long.class)
                .setParameter(parameterName, parameterValue)
                .getSingleResult();
        return count == null ? 0L : count;
    }

    public record StatsOverview(long users,
                                long participants,
                                long organisateurs,
                                long eventsTotal,
                                long eventsPending,
                                long eventsPublished,
                                long ticketsTotal,
                                long ticketsPaid) {
    }
}
