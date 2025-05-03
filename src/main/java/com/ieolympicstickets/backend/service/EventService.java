package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.model.Event;
import com.ieolympicstickets.backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService (EventRepository eventRepository) {
        this.eventRepository=eventRepository;
    }
    public List<Event>findAllEvents() {
        return eventRepository.findAll();
    }
    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id);
    }
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
