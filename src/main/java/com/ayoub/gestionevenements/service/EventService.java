package com.ayoub.gestionevenements.service;

import com.ayoub.gestionevenements.dao.EventDAO;
import com.ayoub.gestionevenements.model.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class EventService {

    @Inject
    private EventDAO eventDAO;

    public List<Event> trouverEvenementsPublies() {
        return eventDAO.trouverEvenementsPublies();
    }

    public Event findById(Long id) {
        return eventDAO.findById(id);
    }

    public Event create(Event event) {
        return eventDAO.create(event);
    }

    public Event update(Event event) {
        return eventDAO.update(event);
    }

    public void delete(Long id) {
        eventDAO.delete(id);
    }
}
