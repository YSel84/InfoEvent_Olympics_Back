package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.Event;
import com.ieolympicstickets.backend.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.findAllEvents();
    }
    //test stuff
    @GetMapping("/test")
    public String testApi() {
        return "API OK";
    }
    //test stuff
    @GetMapping("/force")
    public ResponseEntity<String> force() {
        System.out.println(">>> FORCE");
        return ResponseEntity.ok("Force OK");
    }
    //get event by id
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.findEventById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        System.out.println(">>> Reçu Event:");
        System.out.println(" - title: " + event.getTitle());
        System.out.println(" - imageUrl: " + event.getImageUrl());
        System.out.println(" - location: " + event.getLocation());
        System.out.println(" - date: " + event.getDate());
        System.out.println(" - description: " + event.getDescription());
        Event saved = eventService.saveEvent(event);
        return ResponseEntity.ok(saved);
    }
}
