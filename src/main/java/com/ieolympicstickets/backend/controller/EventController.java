package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.Event;
import com.ieolympicstickets.backend.model.Offer;
import com.ieolympicstickets.backend.service.EventService;
import com.ieolympicstickets.backend.service.OfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final OfferService offerService;

    public EventController(EventService eventService, OfferService offerService) {

        this.eventService = eventService;
        this.offerService = offerService;
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

    @GetMapping("/{id}/offers")
    public ResponseEntity<List<Offer>> getOffersByEvent(@PathVariable Long id) {
        List<Offer> offers=offerService.findOffersByEvent(id);
        return ResponseEntity.ok(offers);
    }

    //
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event saved = eventService.saveEvent(event);
        return ResponseEntity.ok(saved);
    }
}
