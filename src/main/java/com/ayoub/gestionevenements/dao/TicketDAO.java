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
                        "select t from Ticket t where t.participant.id = :userId",
                        Ticket.class)
                .setParameter("userId", userId)
                .getResultList();
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
