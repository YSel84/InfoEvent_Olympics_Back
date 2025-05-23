package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.Ticket;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.service.TicketService;
import com.ieolympicstickets.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Ticket", description = "Service pour la gestion et la consultation des billets")
@RestController
@RequestMapping("/api/tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    public TicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @Operation(
            summary = "Liste des e-billets de l'utilisateur",
            description = "Retourne tous les billets de l'utilisateur courant, classés par événement"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des billets récupérée",
                    content = @Content(schema = @Schema(implementation = TicketDto.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()") // assure que seul un utilisateur connecté puisse accéder
    public ResponseEntity<List<TicketDto>> listMyTickets(Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName());
        List<Ticket> tickets = ticketService.findByUser(user);
        List<TicketDto> dtos = tickets.stream()
                .map(t -> new TicketDto(
                        t.getId(),
                        t.getOffer().getEvent().getId(),
                        t.getOffer().getEvent().getTitle(),
                        t.getOffer().getEvent().getEventDateTime(),
                        t.getQrHash(),
                        t.isUsed(),
                        t.getOrder().getId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(
            summary = "Récupère un e-billet spécifique",
            description = "Retourne le billet identifié par ticketId, si appartenant à l'utilisateur"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billet récupéré",
                    content = @Content(schema = @Schema(implementation = TicketDto.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Billet non trouvé")
    })
    @GetMapping("/{ticketId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketDto> getTicket(
            @PathVariable Long ticketId,
            Authentication auth
    ) {
        User user = userService.findUserByEmail(auth.getName());
        Ticket t = ticketService.getByIdAndUser(ticketId, user);
        TicketDto dto = new TicketDto(
                t.getId(),
                t.getOffer().getEvent().getId(),
                t.getOffer().getEvent().getTitle(),
                t.getOffer().getEvent().getEventDateTime(),
                t.getQrHash(),
                t.isUsed(),
                t.getOrder().getId()
        );
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Scanner un billet",
            description = "Valide le billet correspondant au QR code scanné"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billet scanné avec succès",
                    content = @Content(schema = @Schema(implementation = ScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide ou billet déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé, rôle EMPLOYEE requis"),
            @ApiResponse(responseCode = "404", description = "Billet non trouvé")
    })
    @PostMapping("/scan")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ScanResponse> scanTicket(
            @RequestBody ScanRequest req
    ) {
        boolean success = ticketService.scanByHash(req.getQrHash());
        if (success) {
            return ResponseEntity.ok(new ScanResponse("SCANNED"));
        } else {
            return ResponseEntity.badRequest().body(new ScanResponse("ALREADY_USED"));
        }
    }

    // --- DTO interne pour simplifier la réponse ---
    public static class TicketDto {
        @Schema(description = "ID du billet")
        public Long ticketId;
        @Schema(description = "ID de l'épreuve")
        public Long eventId;
        @Schema(description = "Titre de l'épreuve")
        public String eventTitle;
        @Schema(description = "Date et heure de l'épreuve")
        public LocalDateTime eventDateTime;
        @Schema(description = "Clé QR du billet")
        public String qrHash;
        @Schema(description = "Indique si le billet a été utilisé")
        public boolean used;
        @Schema(description = "ID de la commande associée")
        public Long orderId;

        public TicketDto(Long ticketId, Long eventId, String eventTitle,
                         LocalDateTime eventDateTime, String qrHash,
                         boolean used, Long orderId) {
            this.ticketId = ticketId;
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.eventDateTime = eventDateTime;
            this.qrHash = qrHash;
            this.used = used;
            this.orderId = orderId;
        }
    }

    public static class ScanRequest {
        @Schema(description = "QR hash du billet")
        private String qrHash;
        public String getQrHash() { return qrHash; }
        public void setQrHash(String qrHash) { this.qrHash = qrHash; }
    }

    public static class ScanResponse {
        @Schema(description = "Statut du scan", example = "SCANNED")
        private String status;
        public ScanResponse(String status) { this.status = status; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ErrorResponse {
        @Schema(description = "Message d'erreur", example = "Billet déjà utilisé")
        private String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
