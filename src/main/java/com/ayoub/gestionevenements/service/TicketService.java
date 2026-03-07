package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.dao.TicketDAO;
import com.ayoub.gestionevenements.model.Event;
import com.ayoub.gestionevenements.model.Ticket;
import com.ayoub.gestionevenements.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TicketService {

    @Inject
    private TicketDAO ticketDAO;

    @Inject
    private EntityManager em;

    public Ticket findById(Long id) {
        return ticketDAO.findById(id);
    }

    public List<Ticket> findAll() {
        return ticketDAO.findAll();
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

            event.setCapacite(event.getCapacite() - 1);

            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setParticipant(participant);
            ticket.setPrix(event.getPrix());
            ticket.setStatut(Ticket.Status.RESERVE);
            ticket.setCode(generateTicketCode());

            em.persist(ticket);

            tx.commit();
            return ticket;
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
