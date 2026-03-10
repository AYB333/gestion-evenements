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

    @Inject
    private StripeCheckoutService stripeCheckoutService;

    public record StripeRedirectResult(boolean success, String checkoutUrl, String message) {
    }

    public record PaymentResult(Paiement paiement, String message) {
    }

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

    public boolean isStripeEnabled() {
        return stripeCheckoutService.isEnabled();
    }

    public StripeRedirectResult createStripeCheckout(Long ticketId,
                                                     Long userId,
                                                     String customerEmail,
                                                     String customerName,
                                                     String successUrl,
                                                     String cancelUrl) {
        PaymentContext context = loadTicketForPayment(ticketId, userId);
        if (context == null) {
            return new StripeRedirectResult(false, null, "Ticket invalide.");
        }
        if (!stripeCheckoutService.isEnabled()) {
            return new StripeRedirectResult(false, null, "Stripe n'est pas configure sur le serveur.");
        }
        try {
            StripeCheckoutService.CheckoutSessionResult session = stripeCheckoutService.createCheckoutSession(
                    context.ticket(),
                    userId,
                    customerEmail,
                    customerName,
                    successUrl,
                    cancelUrl);
            if (session == null || session.checkoutUrl() == null || session.checkoutUrl().isBlank()) {
                return new StripeRedirectResult(false, null, "Impossible de creer la session Stripe.");
            }
            return new StripeRedirectResult(true, session.checkoutUrl(), null);
        } catch (Exception ex) {
            return new StripeRedirectResult(false, null, "Erreur Stripe: " + ex.getMessage());
        }
    }

    public Paiement payer(Long ticketId, Long userId, Paiement.Methode methode) {
        if (ticketId == null || userId == null || methode == null) {
            return null;
        }

        // Local payment simulation remains available for card and PayPal.
        return finalizePayment(ticketId, userId, methode, UUID.randomUUID().toString().replace("-", ""));
    }

    public PaymentResult confirmerPaiementStripe(Long ticketId, Long userId, String sessionId) {
        if (ticketId == null || userId == null || sessionId == null || sessionId.isBlank()) {
            return new PaymentResult(null, "Session Stripe invalide.");
        }
        try {
            StripeCheckoutService.StripeVerificationResult verification =
                    stripeCheckoutService.verifyPaidSession(sessionId, ticketId, userId);
            if (!verification.success()) {
                return new PaymentResult(null, verification.message());
            }
            Paiement paiement = finalizePayment(ticketId, userId, Paiement.Methode.STRIPE, verification.paymentReference());
            if (paiement == null) {
                return new PaymentResult(null, "Paiement Stripe deja traite ou ticket invalide.");
            }
            return new PaymentResult(paiement, null);
        } catch (Exception ex) {
            return new PaymentResult(null, "Verification Stripe impossible: " + ex.getMessage());
        }
    }

    private Paiement finalizePayment(Long ticketId, Long userId, Paiement.Methode methode, String reference) {
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

            PaymentContext context = buildPaymentContext(ticket);

            Paiement paiement = new Paiement();
            paiement.setTicket(ticket);
            paiement.setMontant(ticket.getPrix());
            paiement.setMethode(methode);
            paiement.setStatut(Paiement.Statut.SUCCES);
            paiement.setReference(normalizeReference(reference));
            paiement.setPaidAt(LocalDateTime.now());

            ticket.setStatut(Ticket.Status.PAYE);

            em.persist(paiement);
            em.merge(ticket);

            tx.commit();

            ticketEmailService.sendPaymentSuccess(
                    context.participantEmail(),
                    context.participantName(),
                    context.eventTitle(),
                    context.eventDate(),
                    context.ticketCode(),
                    context.montant(),
                    methode.name());
            ticketEmailService.sendOrganizerSaleNotification(
                    context.organizerEmail(),
                    context.organizerName(),
                    context.participantEmail(),
                    context.eventTitle(),
                    context.ticketCode(),
                    context.montant(),
                    methode.name());
            return paiement;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private PaymentContext loadTicketForPayment(Long ticketId, Long userId) {
        if (ticketId == null || userId == null) {
            return null;
        }
        Ticket ticket = em.find(Ticket.class, ticketId);
        if (ticket == null || ticket.getParticipant() == null || ticket.getParticipant().getId() == null) {
            return null;
        }
        if (!userId.equals(ticket.getParticipant().getId())) {
            return null;
        }
        if (ticket.getStatut() != Ticket.Status.RESERVE) {
            return null;
        }
        if (findByTicketId(ticketId) != null) {
            return null;
        }
        return buildPaymentContext(ticket);
    }

    private PaymentContext buildPaymentContext(Ticket ticket) {
        return new PaymentContext(
                ticket,
                ticket.getParticipant().getEmail(),
                ticket.getParticipant().getFullName(),
                ticket.getEvent() == null ? null : ticket.getEvent().getTitre(),
                ticket.getEvent() == null ? null : ticket.getEvent().getDateDebut(),
                ticket.getCode(),
                ticket.getPrix(),
                ticket.getEvent() != null
                        && ticket.getEvent().getOrganisateur() != null
                        && ticket.getEvent().getOrganisateur().getUser() != null
                        ? ticket.getEvent().getOrganisateur().getUser().getEmail()
                        : null,
                ticket.getEvent() != null
                        && ticket.getEvent().getOrganisateur() != null
                        && ticket.getEvent().getOrganisateur().getUser() != null
                        ? ticket.getEvent().getOrganisateur().getUser().getFullName()
                        : null);
    }

    private String normalizeReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        String normalized = reference.trim();
        return normalized.length() <= 100 ? normalized : normalized.substring(0, 100);
    }

    private record PaymentContext(Ticket ticket,
                                  String participantEmail,
                                  String participantName,
                                  String eventTitle,
                                  LocalDateTime eventDate,
                                  String ticketCode,
                                  java.math.BigDecimal montant,
                                  String organizerEmail,
                                  String organizerName) {
    }
}
