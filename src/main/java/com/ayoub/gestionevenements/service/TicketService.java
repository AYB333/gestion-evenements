package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.dao.TicketDAO;
import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import com.ayoub.gestionevenements.dao.UserDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class TicketService {

    public record TransferResult(boolean success, String message) {}

    @Inject
    private TicketDAO ticketDAO;

    @Inject
    private EntityManager em;

    @Inject
    private UserDAO userDAO;

    @Inject
    private TicketEmailService ticketEmailService;

    public Ticket findById(Long id) {
        return ticketDAO.findById(id);
    }

    public Ticket findByIdWithEvent(Long id) {
        return ticketDAO.findByIdWithEvent(id);
    }

    public List<Ticket> findAll() {
        return ticketDAO.findAll();
    }

    public long countAll() {
        return ticketDAO.countAll();
    }

    public long countByStatus(Ticket.Status status) {
        return ticketDAO.countByStatus(status);
    }

    public long countByEvent(Long eventId) {
        return ticketDAO.countByEvent(eventId);
    }

    public long countByEventAndStatus(Long eventId, Ticket.Status status) {
        return ticketDAO.countByEventAndStatus(eventId, status);
    }

    public Ticket create(Ticket ticket) {
        return ticketDAO.create(ticket);
    }

    public Ticket update(Ticket ticket) {
        return ticketDAO.update(ticket);
    }

    public void delete(Long id) {
        ticketDAO.delete(id);
    }

    public List<Ticket> trouverTicketsParParticipant(Long userId) {
        return ticketDAO.trouverTicketsParParticipant(userId);
    }

    public List<Ticket> trouverTicketsParParticipant(Long userId, Ticket.Status statut, int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        int offset = (page - 1) * pageSize;
        return ticketDAO.trouverTicketsParParticipant(userId, statut, offset, pageSize);
    }

    public long countTicketsParParticipant(Long userId, Ticket.Status statut) {
        return ticketDAO.countTicketsParParticipant(userId, statut);
    }

    public long countByOrganisateur(Long organisateurId) {
        return ticketDAO.countByOrganisateur(organisateurId);
    }

    public long countByOrganisateurAndStatus(Long organisateurId, Ticket.Status statut) {
        return ticketDAO.countByOrganisateurAndStatus(organisateurId, statut);
    }

    public java.math.BigDecimal sumRevenueByOrganisateur(Long organisateurId) {
        return ticketDAO.sumRevenueByOrganisateur(organisateurId);
    }

    public java.math.BigDecimal sumRevenueByEvent(Long eventId) {
        return ticketDAO.sumRevenueByEvent(eventId);
    }

    public List<TicketDAO.EventSalesRow> findTopEventsByPaidTickets(int limit) {
        return ticketDAO.findTopEventsByPaidTickets(limit);
    }

    public boolean dejaReserve(Long eventId, Long userId) {
        if (eventId == null || userId == null) {
            return false;
        }
        return ticketDAO.existsForEventAndParticipant(eventId, userId);
    }

    public Ticket reserverTicket(Long eventId, Long userId) {
        if (eventId == null || userId == null) {
            return null;
        }

        // Lock the event row before touching capacity so concurrent reservations
        // cannot oversell the last available places.
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Event event = em.find(Event.class, eventId, LockModeType.PESSIMISTIC_WRITE);
            if (event == null || event.getCapacite() == null || event.getCapacite() <= 0) {
                tx.rollback();
                return null;
            }

            if (event.getStatut() != Event.Status.PUBLIE) {
                tx.rollback();
                return null;
            }
            if (event.getDateDebut() != null && event.getDateDebut().isBefore(java.time.LocalDateTime.now())) {
                tx.rollback();
                return null;
            }

            User participant = em.find(User.class, userId);
            if (participant == null) {
                tx.rollback();
                return null;
            }

            if (participant.getRole() != User.Role.PARTICIPANT) {
                tx.rollback();
                return null;
            }

            if (event.getOrganisateur() != null
                    && event.getOrganisateur().getUser() != null
                    && userId.equals(event.getOrganisateur().getUser().getId())) {
                tx.rollback();
                return null;
            }

            if (ticketDAO.existsForEventAndParticipant(eventId, userId)) {
                tx.rollback();
                return null;
            }

            String organizerEmail = event.getOrganisateur() != null
                    && event.getOrganisateur().getUser() != null
                    ? event.getOrganisateur().getUser().getEmail()
                    : null;
            String organizerName = event.getOrganisateur() != null
                    && event.getOrganisateur().getUser() != null
                    ? event.getOrganisateur().getUser().getFullName()
                    : null;
            String participantEmail = participant.getEmail();
            String participantName = participant.getFullName();
            String eventTitle = event.getTitre();
            java.time.LocalDateTime eventDate = event.getDateDebut();

            event.setCapacite(event.getCapacite() - 1);

            Ticket reusableTicket = ticketDAO.findReusableCancelledTicket(eventId, userId);
            if (reusableTicket != null) {
                reusableTicket.setEvent(event);
                reusableTicket.setParticipant(participant);
                reusableTicket.setPrix(event.getPrix());
                reusableTicket.setStatut(Ticket.Status.RESERVE);
                if (reusableTicket.getCode() == null || reusableTicket.getCode().isBlank()) {
                    reusableTicket.setCode(generateTicketCode());
                }

                em.merge(reusableTicket);
                em.merge(event);
                tx.commit();
                ticketEmailService.sendOrganizerReservationNotification(
                        organizerEmail,
                        organizerName,
                        participantEmail,
                        participantName,
                        eventTitle,
                        eventDate,
                        reusableTicket.getCode(),
                        reusableTicket.getPrix());
                return reusableTicket;
            }

            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setParticipant(participant);
            ticket.setPrix(event.getPrix());
            ticket.setStatut(Ticket.Status.RESERVE);
            ticket.setCode(generateTicketCode());

            em.persist(ticket);

            tx.commit();
            ticketEmailService.sendOrganizerReservationNotification(
                    organizerEmail,
                    organizerName,
                    participantEmail,
                    participantName,
                    eventTitle,
                    eventDate,
                    ticket.getCode(),
                    ticket.getPrix());
            return ticket;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public boolean annulerTicket(Long ticketId, Long userId) {
        if (ticketId == null || userId == null) {
            return false;
        }

        // Cancellation returns one seat to the event and notifies participant/organizer.
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Ticket ticket = em.find(Ticket.class, ticketId, LockModeType.PESSIMISTIC_WRITE);
            if (ticket == null || ticket.getParticipant() == null || ticket.getParticipant().getId() == null) {
                tx.rollback();
                return false;
            }
            if (!userId.equals(ticket.getParticipant().getId())) {
                tx.rollback();
                return false;
            }
            if (ticket.getStatut() != Ticket.Status.RESERVE && ticket.getStatut() != Ticket.Status.PAYE) {
                tx.rollback();
                return false;
            }

            Event event = ticket.getEvent();
            if (event == null || event.getId() == null) {
                tx.rollback();
                return false;
            }
            Event lockedEvent = em.find(Event.class, event.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (lockedEvent == null) {
                tx.rollback();
                return false;
            }
            if (lockedEvent.getDateDebut() != null && lockedEvent.getDateDebut().isBefore(java.time.LocalDateTime.now())) {
                tx.rollback();
                return false;
            }

            String participantEmail = ticket.getParticipant().getEmail();
            String participantName = ticket.getParticipant().getFullName();
            String eventTitle = lockedEvent.getTitre();
            java.time.LocalDateTime eventDate = lockedEvent.getDateDebut();
            String ticketCode = ticket.getCode();
            String organizerEmail = lockedEvent.getOrganisateur() != null
                    && lockedEvent.getOrganisateur().getUser() != null
                    ? lockedEvent.getOrganisateur().getUser().getEmail()
                    : null;
            String organizerName = lockedEvent.getOrganisateur() != null
                    && lockedEvent.getOrganisateur().getUser() != null
                    ? lockedEvent.getOrganisateur().getUser().getFullName()
                    : null;

            ticket.setStatut(Ticket.Status.ANNULE);
            Integer currentCapacity = lockedEvent.getCapacite();
            lockedEvent.setCapacite((currentCapacity == null ? 0 : currentCapacity) + 1);

            em.merge(ticket);
            em.merge(lockedEvent);

            tx.commit();

            ticketEmailService.sendTicketCancelledParticipant(
                    participantEmail,
                    participantName,
                    eventTitle,
                    eventDate,
                    ticketCode);
            ticketEmailService.sendOrganizerCancellationNotification(
                    organizerEmail,
                    organizerName,
                    participantEmail,
                    eventTitle,
                    ticketCode);
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public boolean transfererTicket(Long ticketId, Long sourceUserId, String targetEmail) {
        return transfererTicketAvecMessage(ticketId, sourceUserId, targetEmail).success();
    }

    public TransferResult transfererTicketAvecMessage(Long ticketId, Long sourceUserId, String targetEmail) {
        if (ticketId == null || sourceUserId == null || targetEmail == null || targetEmail.isBlank()) {
            return new TransferResult(false, "Email cible invalide.");
        }

        // The old ticket stays as an audit trail (TRANSFERE) and a fresh ticket
        // is created for the new participant.
        String normalizedEmail = targetEmail.trim().toLowerCase(Locale.ROOT);
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Ticket ticket = em.find(Ticket.class, ticketId, LockModeType.PESSIMISTIC_WRITE);
            if (ticket == null || ticket.getParticipant() == null || ticket.getParticipant().getId() == null) {
                tx.rollback();
                return new TransferResult(false, "Ticket introuvable.");
            }
            if (!sourceUserId.equals(ticket.getParticipant().getId())) {
                tx.rollback();
                return new TransferResult(false, "Vous n'etes pas proprietaire de ce ticket.");
            }
            if (ticket.getStatut() != Ticket.Status.RESERVE && ticket.getStatut() != Ticket.Status.PAYE) {
                tx.rollback();
                return new TransferResult(false, "Seuls les tickets reserves ou payes peuvent etre transferes.");
            }
            if (ticket.getEvent() == null || ticket.getEvent().getId() == null) {
                tx.rollback();
                return new TransferResult(false, "Evenement introuvable.");
            }
            if (ticket.getEvent().getDateDebut() != null && ticket.getEvent().getDateDebut().isBefore(java.time.LocalDateTime.now())) {
                tx.rollback();
                return new TransferResult(false, "Impossible: l'evenement est deja passe.");
            }

            User target = userDAO.findByEmail(normalizedEmail);
            if (target == null) {
                tx.rollback();
                return new TransferResult(false, "Compte destinataire introuvable.");
            }
            if (!target.isEnabled()) {
                tx.rollback();
                return new TransferResult(false, "Compte destinataire desactive.");
            }
            if (target.getRole() != User.Role.PARTICIPANT) {
                tx.rollback();
                return new TransferResult(false, "Le destinataire doit etre un participant.");
            }
            if (target.getId().equals(sourceUserId)) {
                tx.rollback();
                return new TransferResult(false, "Vous ne pouvez pas vous transferer le ticket a vous-meme.");
            }
            if (ticketDAO.existsForEventAndParticipant(ticket.getEvent().getId(), target.getId())) {
                tx.rollback();
                return new TransferResult(false, "Ce participant a deja un ticket actif pour cet evenement.");
            }

            Ticket.Status newStatus = ticket.getStatut() == Ticket.Status.PAYE ? Ticket.Status.PAYE : Ticket.Status.RESERVE;

            String sourceEmail = ticket.getParticipant().getEmail();
            String sourceName = ticket.getParticipant().getFullName();
            String eventTitle = ticket.getEvent().getTitre();
            java.time.LocalDateTime eventDate = ticket.getEvent().getDateDebut();
            BigDecimal montant = ticket.getPrix();
            String oldCode = ticket.getCode();
            String organizerEmail = ticket.getEvent().getOrganisateur() != null
                    && ticket.getEvent().getOrganisateur().getUser() != null
                    ? ticket.getEvent().getOrganisateur().getUser().getEmail()
                    : null;
            String organizerName = ticket.getEvent().getOrganisateur() != null
                    && ticket.getEvent().getOrganisateur().getUser() != null
                    ? ticket.getEvent().getOrganisateur().getUser().getFullName()
                    : null;

            ticket.setStatut(Ticket.Status.TRANSFERE);
            em.merge(ticket);

            Ticket newTicket = new Ticket();
            newTicket.setCode(generateTicketCode());
            newTicket.setEvent(ticket.getEvent());
            newTicket.setParticipant(target);
            newTicket.setPrix(ticket.getPrix());
            newTicket.setStatut(newStatus);
            em.persist(newTicket);

            tx.commit();

            ticketEmailService.sendTicketTransferSentParticipant(
                    sourceEmail,
                    sourceName,
                    eventTitle,
                    target.getEmail(),
                    oldCode);
            ticketEmailService.sendTicketTransferReceivedParticipant(
                    target.getEmail(),
                    target.getFullName(),
                    eventTitle,
                    eventDate,
                    newTicket.getCode(),
                    montant,
                    sourceEmail,
                    newStatus == Ticket.Status.PAYE);
            ticketEmailService.sendOrganizerTransferNotification(
                    organizerEmail,
                    organizerName,
                    sourceEmail,
                    target.getEmail(),
                    eventTitle,
                    oldCode,
                    newTicket.getCode());
            return new TransferResult(true, "Billet transfere avec succes.");
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private String generateTicketCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
