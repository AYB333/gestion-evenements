package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.model.Organisateur;
import com.ayoub.gestionevenements.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

@ApplicationScoped
public class AdminUserManagementService {

    public record Result(boolean success, String message) {}

    @Inject
    private EntityManager em;

    public Result updateRole(Long targetUserId, Long currentAdminId, User.Role targetRole) {
        if (targetUserId == null || targetRole == null) {
            return new Result(false, "Role ou utilisateur invalide.");
        }
        if (targetRole == User.Role.ADMIN) {
            return new Result(false, "La promotion admin n'est pas autorisee depuis cette interface.");
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = em.find(User.class, targetUserId, LockModeType.PESSIMISTIC_WRITE);
            if (user == null) {
                tx.rollback();
                return new Result(false, "Utilisateur introuvable.");
            }
            if (user.getRole() == User.Role.ADMIN || (currentAdminId != null && currentAdminId.equals(user.getId()))) {
                tx.rollback();
                return new Result(false, "Ce compte est protege.");
            }
            if (user.getRole() == targetRole) {
                tx.rollback();
                return new Result(true, "Aucun changement de role necessaire.");
            }

            Organisateur organisateur = em.find(Organisateur.class, user.getId(), LockModeType.PESSIMISTIC_WRITE);

            if (targetRole == User.Role.ORGANISATEUR) {
                user.setRole(User.Role.ORGANISATEUR);
                if (organisateur == null) {
                    organisateur = new Organisateur();
                    organisateur.setUser(user);
                    organisateur.setOrganisationName(defaultOrganisationName(user));
                    organisateur.setTelephone(null);
                    em.persist(organisateur);
                }
                em.merge(user);
                tx.commit();
                return new Result(true, "Role mis a jour: organisateur.");
            }

            long eventCount = countEventsForUser(user.getId());
            if (eventCount > 0) {
                tx.rollback();
                return new Result(false, "Impossible: cet organisateur possede deja des evenements.");
            }

            user.setRole(User.Role.PARTICIPANT);
            em.merge(user);
            if (organisateur != null) {
                em.remove(organisateur);
            }

            tx.commit();
            return new Result(true, "Role mis a jour: participant.");
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private long countEventsForUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        Long count = em.createQuery(
                        "select count(e) from Event e where e.organisateur.id = :organisateurId",
                        Long.class)
                .setParameter("organisateurId", userId)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    private String defaultOrganisationName(User user) {
        if (user == null || user.getFullName() == null || user.getFullName().isBlank()) {
            return "Organisation";
        }
        return "Organisation " + user.getFullName().trim();
    }
}
