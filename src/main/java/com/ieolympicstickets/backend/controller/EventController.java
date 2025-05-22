package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.Event;
import com.ieolympicstickets.backend.model.Offer;
import com.ieolympicstickets.backend.service.EventService;
import com.ieolympicstickets.backend.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/events")
@Tag(name= "Events", description ="Opérations sur les épreuves")
public class EventController {
    private final EventService eventService;
    private final OfferService offerService;

    public EventController(EventService eventService, OfferService offerService) {

        this.eventService = eventService;
        this.offerService = offerService;
    }

    @GetMapping
    @Operation(summary = "Récupère les épreuves", description = "Retourne la liste de toutes les épreuves en base")
    @ApiResponse(responseCode = "200", description = "Liste d'épreuves renvoyée",
            content = @Content(mediaType="application/json", schema = @Schema(implementation = Event.class)))
    public List<Event> getAllEvents() {
        return eventService.findAllEvents();
    }
    //test stuff
    @GetMapping("/test")
    @Operation(summary = "Point de test de l'API", description = "Vérifie la connexion à l'API")
    @ApiResponse(responseCode = "200", description = "API OK",
            content = @Content(mediaType = "text/plain"))
    public String testApi() {
        return "API OK";
    }

    //get event by id
    @GetMapping("/{id}")
    @Operation(summary = "Récupère une épreuve par son Id", description = "Renvoie une épreuve si l'Id existe, sinon 404")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Épreuve trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Épreuve non trouvée")
    })
    public ResponseEntity<Event> getEventById(
            @Parameter(description = "ID de l'épreuve à récupérer", required = true)
            @PathVariable Long id) {
        Optional<Event> event = eventService.findEventById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/offers")
    @Operation(summary = "Liste des offres d'une épreuve",
            description = "Renvoie les offres associées à une épreuve donné")
    @ApiResponse(responseCode = "200", description = "Liste d'offres renvoyée",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Offer.class)))
    public ResponseEntity<List<Offer>> getOffersByEvent(
            @Parameter(description = "ID de l'épreuve pour lequel lister les offres", required = true)
            @PathVariable Long id) {
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
