package com.ayoub.gestionevenements.dao;

import com.ayoub.gestionevenements.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

@ApplicationScoped
public class TicketDAO {

    public record EventSalesRow(Long eventId, String titre, long ticketsVendus, BigDecimal revenus) {}

    @Inject
    private EntityManager em;

    public Ticket create(Ticket ticket) {
        executeInTransaction(() -> em.persist(ticket));
        return ticket;
    }

    public Ticket findById(Long id) {
        return em.find(Ticket.class, id);
    }

    public Ticket findByIdWithEvent(Long id) {
        if (id == null) {
            return null;
        }
        return em.createQuery(
                        "select t from Ticket t join fetch t.event e where t.id = :id",
                        Ticket.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<Ticket> findAll() {
        return em.createQuery("select t from Ticket t", Ticket.class)
                .getResultList();
    }

    public long countAll() {
        Long count = em.createQuery("select count(t) from Ticket t", Long.class)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public long countByStatus(Ticket.Status status) {
        if (status == null) {
            return 0;
        }
        Long count = em.createQuery("select count(t) from Ticket t where t.statut = :statut", Long.class)
                .setParameter("statut", status)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public long countByEvent(Long eventId) {
        if (eventId == null) {
            return 0;
        }
        Long count = em.createQuery(
                        "select count(t) from Ticket t where t.event.id = :eventId",
                        Long.class)
                .setParameter("eventId", eventId)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public long countByEventAndStatus(Long eventId, Ticket.Status status) {
        if (eventId == null || status == null) {
            return 0;
        }
        Long count = em.createQuery(
                        "select count(t) from Ticket t where t.event.id = :eventId and t.statut = :status",
                        Long.class)
                .setParameter("eventId", eventId)
                .setParameter("status", status)
                .getSingleResult();
        return count == null ? 0 : count;
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

    public long countByOrganisateur(Long organisateurId) {
        if (organisateurId == null) {
            return 0;
        }
        Long count = em.createQuery(
                        "select count(t) from Ticket t where t.event.organisateur.id = :orgId",
                        Long.class)
                .setParameter("orgId", organisateurId)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public long countByOrganisateurAndStatus(Long organisateurId, Ticket.Status statut) {
        if (organisateurId == null || statut == null) {
            return 0;
        }
        Long count = em.createQuery(
                        "select count(t) from Ticket t where t.event.organisateur.id = :orgId and t.statut = :statut",
                        Long.class)
                .setParameter("orgId", organisateurId)
                .setParameter("statut", statut)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public java.math.BigDecimal sumRevenueByOrganisateur(Long organisateurId) {
        if (organisateurId == null) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal sum = em.createQuery(
                        "select coalesce(sum(t.prix), 0) from Ticket t " +
                                "where t.event.organisateur.id = :orgId and t.statut = :statut",
                        java.math.BigDecimal.class)
                .setParameter("orgId", organisateurId)
                .setParameter("statut", Ticket.Status.PAYE)
                .getSingleResult();
        return sum == null ? java.math.BigDecimal.ZERO : sum;
    }

    public List<EventSalesRow> findTopEventsByPaidTickets(int limit) {
        if (limit < 1) {
            limit = 5;
        }
        List<Object[]> rows = em.createQuery(
                        "select e.id, e.titre, count(t), coalesce(sum(t.prix), 0) " +
                                "from Ticket t join t.event e " +
                                "where t.statut = :paidStatus " +
                                "group by e.id, e.titre " +
                                "order by count(t) desc",
                        Object[].class)
                .setParameter("paidStatus", Ticket.Status.PAYE)
                .setMaxResults(limit)
                .getResultList();

        return rows.stream()
                .map(row -> new EventSalesRow(
                        (Long) row[0],
                        (String) row[1],
                        row[2] == null ? 0 : ((Long) row[2]),
                        row[3] == null ? BigDecimal.ZERO : (BigDecimal) row[3]))
                .toList();
    }

    public boolean existsForEventAndParticipant(Long eventId, Long userId) {
        Long count = em.createQuery(
                        "select count(t) from Ticket t " +
                                "where t.event.id = :eventId and t.participant.id = :userId " +
                                "and t.statut in (:reserveStatus, :paidStatus)",
                        Long.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", userId)
                .setParameter("reserveStatus", Ticket.Status.RESERVE)
                .setParameter("paidStatus", Ticket.Status.PAYE)
                .getSingleResult();
        return count != null && count > 0;
    }

    public Ticket findReusableCancelledTicket(Long eventId, Long userId) {
        if (eventId == null || userId == null) {
            return null;
        }
        return em.createQuery(
                        "select t from Ticket t " +
                                "where t.event.id = :eventId and t.participant.id = :userId " +
                                "and t.statut = :cancelledStatus and t.paiement is null " +
                                "order by t.createdAt desc",
                        Ticket.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", userId)
                .setParameter("cancelledStatus", Ticket.Status.ANNULE)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
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
