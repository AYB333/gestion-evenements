package com.ayoub.gestionevenements.dao;

import com.ayoub.gestionevenements.model.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.function.Supplier;

@ApplicationScoped
public class EventDAO {

    @Inject
    private EntityManager em;

    public Event create(Event event) {
        executeInTransaction(() -> em.persist(event));
        return event;
    }

    public Event findById(Long id) {
        return em.find(Event.class, id);
    }

    public List<Event> findAll() {
        return em.createQuery("select e from Event e", Event.class)
                .getResultList();
    }

    public Event update(Event event) {
        return executeInTransaction(() -> em.merge(event));
    }

    public void delete(Long id) {
        executeInTransaction(() -> {
            Event managed = em.find(Event.class, id);
            if (managed != null) {
                em.remove(managed);
            }
        });
    }

    public List<Event> trouverEvenementsPublies() {
        return em.createQuery("select e from Event e where e.statut = :statut", Event.class)
                .setParameter("statut", Event.Status.PUBLIE)
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
