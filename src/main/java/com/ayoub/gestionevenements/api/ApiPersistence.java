package com.ayoub.gestionevenements.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

final class ApiPersistence {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("gestion-evenements-pu");

    private ApiPersistence() {
    }

    static EntityManager createEntityManager() {
        return EMF.createEntityManager();
    }
}

