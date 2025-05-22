package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Ticket", description = "Service pour la validation des billets")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(
            summary = "Scanner un billet",
            description = "Valide le billet correspondant au QR code scanné"
    )
    @ApiResponses(value = {
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
            @RequestBody(description = "Données du scan", required = true,
                    content = @Content(schema = @Schema(implementation = ScanRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody ScanRequest req
    ) {
        // TODO: appeler ticketService.scanTicket et gérer les cas
        boolean success = ticketService.scanTicket(req.getTicketId());
        if (success) {
            return ResponseEntity.ok(new ScanResponse("SCANNED"));
        } else {
            return ResponseEntity.badRequest().body(new ScanResponse("ALREADY_USED"));
        }
    }

    public static class ScanRequest {
        @Schema(description = "ID du billet", example = "123")
        private Long ticketId;

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    }

    public static class ScanResponse {
        @Schema(description = "Status du scan", example = "SCANNED")
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