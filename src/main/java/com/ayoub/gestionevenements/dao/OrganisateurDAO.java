package com.ayoub.gestionevenements.dao;

import com.ayoub.gestionevenements.model.Organisateur;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Supplier;

@ApplicationScoped
public class OrganisateurDAO {

    @Inject
    private EntityManager em;

    public Organisateur create(Organisateur organisateur) {
        executeInTransaction(() -> em.persist(organisateur));
        return organisateur;
    }

    public Organisateur findById(Long id) {
        return em.find(Organisateur.class, id);
    }

    public Organisateur findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return em.createQuery("select o from Organisateur o where o.user.id = :userId", Organisateur.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Organisateur update(Organisateur organisateur) {
        return executeInTransaction(() -> em.merge(organisateur));
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
