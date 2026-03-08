package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.model.Paiement;
import com.ayoub.gestionevenements.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class PaiementService {

    @Inject
    private EntityManager em;

    @Inject
    private TicketEmailService ticketEmailService;

    public Paiement findByTicketId(Long ticketId) {
        if (ticketId == null) {
            return null;
        }
        return em.createQuery(
                        "select p from Paiement p where p.ticket.id = :ticketId",
                        Paiement.class)
                .setParameter("ticketId", ticketId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Paiement payer(Long ticketId, Long userId, Paiement.Methode methode) {
        if (ticketId == null || userId == null || methode == null) {
            return null;
        }

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Ticket ticket = em.find(Ticket.class, ticketId, LockModeType.PESSIMISTIC_WRITE);
            if (ticket == null || ticket.getParticipant() == null || ticket.getParticipant().getId() == null) {
                tx.rollback();
                return null;
            }
            if (!userId.equals(ticket.getParticipant().getId())) {
                tx.rollback();
                return null;
            }
            if (ticket.getStatut() != Ticket.Status.RESERVE) {
                tx.rollback();
                return null;
            }

            Long existing = em.createQuery(
                            "select count(p) from Paiement p where p.ticket.id = :ticketId",
                            Long.class)
                    .setParameter("ticketId", ticketId)
                    .getSingleResult();
            if (existing != null && existing > 0) {
                tx.rollback();
                return null;
            }

            String email = ticket.getParticipant().getEmail();
            String fullName = ticket.getParticipant().getFullName();
            String eventTitle = ticket.getEvent() == null ? null : ticket.getEvent().getTitre();
            var eventDate = ticket.getEvent() == null ? null : ticket.getEvent().getDateDebut();
            String ticketCode = ticket.getCode();
            String organizerEmail = ticket.getEvent() != null
                    && ticket.getEvent().getOrganisateur() != null
                    && ticket.getEvent().getOrganisateur().getUser() != null
                    ? ticket.getEvent().getOrganisateur().getUser().getEmail()
                    : null;
            String organizerName = ticket.getEvent() != null
                    && ticket.getEvent().getOrganisateur() != null
                    && ticket.getEvent().getOrganisateur().getUser() != null
                    ? ticket.getEvent().getOrganisateur().getUser().getFullName()
                    : null;

            Paiement paiement = new Paiement();
            paiement.setTicket(ticket);
            paiement.setMontant(ticket.getPrix());
            paiement.setMethode(methode);
            paiement.setStatut(Paiement.Statut.SUCCES);
            paiement.setReference(UUID.randomUUID().toString().replace("-", ""));
            paiement.setPaidAt(LocalDateTime.now());

            ticket.setStatut(Ticket.Status.PAYE);

            em.persist(paiement);
            em.merge(ticket);

            tx.commit();

            ticketEmailService.sendPaymentSuccess(
                    email,
                    fullName,
                    eventTitle,
                    eventDate,
                    ticketCode,
                    ticket.getPrix(),
                    methode.name());
            ticketEmailService.sendOrganizerSaleNotification(
                    organizerEmail,
                    organizerName,
                    email,
                    eventTitle,
                    ticketCode,
                    ticket.getPrix(),
                    methode.name());
            return paiement;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }
}
