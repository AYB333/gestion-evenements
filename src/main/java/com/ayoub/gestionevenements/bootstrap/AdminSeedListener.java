package com.ayoub.gestionevenements.bootstrap;

import com.ayoub.gestionevenements.model.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

@WebListener
public class AdminSeedListener implements ServletContextListener {

    private static final String ADMIN_EMAIL = "admin@admin.ma";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            emf = Persistence.createEntityManagerFactory("gestion-evenements-pu");
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            User existing = em.createQuery(
                            "select u from User u where u.email = :email", User.class)
                    .setParameter("email", ADMIN_EMAIL)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                User admin = new User();
                admin.setFullName("Admin");
                admin.setEmail(ADMIN_EMAIL);
                admin.setRole(User.Role.ADMIN);
                // Store plaintext; first login will migrate to hashed password.
                admin.setPasswordHash(ADMIN_PASSWORD);
                admin.setEnabled(true);
                em.persist(admin);
            }

            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            // Do not block app startup if seeding fails.
            System.err.println("Admin seed failed: " + ex.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) {
                emf.close();
            }
        }
    }
}
