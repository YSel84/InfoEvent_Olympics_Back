package com.ieolympicstickets.backend.controller;


import com.ieolympicstickets.backend.model.Event;
import com.ieolympicstickets.backend.model.Offer;
import com.ieolympicstickets.backend.model.Role;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.service.EventService;
import com.ieolympicstickets.backend.service.OfferService;
import com.ieolympicstickets.backend.service.OrderService;
import com.ieolympicstickets.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final OfferService offerService;
    private final OrderService orderService;

    public AdminController(UserService userService,
                                              EventService eventService,
                                              OfferService offerService,
                                              OrderService orderService) {
                    this.userService = userService;
                    this.eventService = eventService;
                    this.offerService = offerService;
                    this.orderService = orderService;
    }

    //CRUD offers

    @GetMapping("/offers")
    public List<Offer> listOffers() {
        return offerService.findAllOffers();
    }

    @PostMapping("/offers")
    public ResponseEntity<Offer> createOffer(
            @Valid @RequestBody Offer offer
    ) {
        Offer saved = offerService.saveOffer(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/offers/{id}")
    public Offer updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody Offer offer
    ) {
        offer.setOfferId(id);
        return offerService.saveOffer(offer);
    }

    @DeleteMapping("/offers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
    }

    // CRUD events

    @GetMapping ("/events")
    public List<Event> listEvents() {
        return eventService.findAllEvents();
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody Event event
    ) {
        Event saved = eventService.saveEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/events/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody Event event
    ) {
        event.setId(id);
        return eventService.saveEvent(event);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    // sales
    @GetMapping("/offers/{id}/sales")
    public ResponseEntity<Long> getItemsSoldByOffer(@PathVariable Long id) {
        long count = orderService.countItemsSoldByOffer(id);
        return ResponseEntity.ok(count);
    }

// User operations - to expand
    /**
     * Fetch user_key
     * */
    @GetMapping("/users/{id}/key")
    public ResponseEntity<String> getUserKey(@PathVariable Long id) {
            User u = userService.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
            return ResponseEntity.ok(u.getUserKey());
    }

    @PatchMapping("/users/{id}/role")
     public ResponseEntity<User> changeUserRole(
            @PathVariable Long id,
            @RequestParam Role role
     ) {
            User u = userService.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
            u.setRole(role);
            User updated = userService.register(u, u.getPasswordHash());
            return ResponseEntity.ok(updated);
     }
}
