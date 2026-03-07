package com.ayoub.gestionevenements.dao;

import com.ayoub.gestionevenements.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.function.Supplier;

@ApplicationScoped
public class TicketDAO {

    @Inject
    private EntityManager em;

    public Ticket create(Ticket ticket) {
        executeInTransaction(() -> em.persist(ticket));
        return ticket;
    }

    public Ticket findById(Long id) {
        return em.find(Ticket.class, id);
    }

    public List<Ticket> findAll() {
        return em.createQuery("select t from Ticket t", Ticket.class)
                .getResultList();
    }

    public Ticket update(Ticket ticket) {
        return executeInTransaction(() -> em.merge(ticket));
    }

    public void delete(Long id) {
        executeInTransaction(() -> {
            Ticket managed = em.find(Ticket.class, id);
            if (managed != null) {
                em.remove(managed);
            }
        });
    }

    public List<Ticket> trouverTicketsParParticipant(Long userId) {
        return em.createQuery(
                        "select t from Ticket t join fetch t.event e " +
                                "where t.participant.id = :userId " +
                                "order by t.createdAt desc",
                        Ticket.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Ticket> trouverTicketsParParticipant(Long userId, Ticket.Status statut, int offset, int limit) {
        StringBuilder jpql = new StringBuilder(
                "select t from Ticket t join fetch t.event e " +
                        "where t.participant.id = :userId ");
        if (statut != null) {
            jpql.append("and t.statut = :statut ");
        }
        jpql.append("order by t.createdAt desc");

        var query = em.createQuery(jpql.toString(), Ticket.class)
                .setParameter("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(limit);
        if (statut != null) {
            query.setParameter("statut", statut);
        }
        return query.getResultList();
    }

    public long countTicketsParParticipant(Long userId, Ticket.Status statut) {
        StringBuilder jpql = new StringBuilder(
                "select count(t) from Ticket t where t.participant.id = :userId ");
        if (statut != null) {
            jpql.append("and t.statut = :statut ");
        }
        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("userId", userId);
        if (statut != null) {
            query.setParameter("statut", statut);
        }
        Long count = query.getSingleResult();
        return count == null ? 0 : count;
    }

    public boolean existsForEventAndParticipant(Long eventId, Long userId) {
        Long count = em.createQuery(
                        "select count(t) from Ticket t where t.event.id = :eventId and t.participant.id = :userId",
                        Long.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count != null && count > 0;
    }

    private void executeInTransaction(Runnable action) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.run();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private <T> T executeInTransaction(Supplier<T> action) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = action.get();
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }
}
