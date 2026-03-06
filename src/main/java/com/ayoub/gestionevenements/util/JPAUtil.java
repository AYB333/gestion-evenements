package com.ayoub.gestionevenements.util;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class JPAUtil {

    private static final String PU_NAME = "gestion-evenements-pu";
    private final EntityManagerFactory emf;

    public JPAUtil() {
        this.emf = Persistence.createEntityManagerFactory(PU_NAME);
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void close(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
