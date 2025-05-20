package com.ieolympicstickets.backend.controller;


import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.service.CartService;
import com.ieolympicstickets.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Gestion du panier et du tunnel de paiement")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController (CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }


    @Operation(summary = "Valide le panier et génère les billets (mock)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panier validé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidateCartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erreur de validation du panier"),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    @PostMapping("/validate")
    public ResponseEntity<ValidateCartResponse> validateCart(
            @RequestBody ValidateCartRequest request,
            Authentication authentication) {
            //use email from JWT
            String email = authentication.getName();
            //load User if needed
            User user = userService.findUserByEmail(email);
            //ask service
            ValidateCartResponse resp = cartService.validateCart(request.cartId(), user);
            return ResponseEntity.ok(resp);
    }


    @Operation(summary = "Crée ou récupère un panier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panier créé ou récupéré avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    //create or get cart
    @PostMapping
    public ResponseEntity<CartResponse> createOrGetCart(
            @RequestHeader(name="X-Session-Id", required = false) String sessionId,
            Authentication authentication
    ) {
        //connected user or null for guest
        User user = (authentication != null && authentication.isAuthenticated())
                ? userService.findUserByEmail(authentication.getName()) : null;
        // get or create cart
        Cart cart = cartService.getOrCreateCart(sessionId, user);
        return ResponseEntity.ok(new CartResponse(cart.getId()));
    }

    @Operation(summary = "Récupère un panier existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panier récupéré",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Panier introuvable"),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    //get cart
    @GetMapping
    public ResponseEntity<CartDetailsResponse> getCart(
            @RequestHeader(name ="X-Session-Id", required = false) String sessionId,
            Authentication authentication
    ) {
        //Connected or guest?
        User user = (authentication !=null && authentication.isAuthenticated())
                ?userService.findUserByEmail(authentication.getName()) : null;
        //fetch existing cart or no content
        Optional<Cart> optCart = cartService.getCart(sessionId, user);
        if (optCart.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Cart cart = optCart.get();
        //map CartItems to DTO
        List<CartItemDetailsDto> items = cart.getItems().stream()
                .map(item -> new CartItemDetailsDto(
                        item.getId(),
                        item.getOffer().getOfferId(),
                        item.getQuantity(),
                        item.getOffer().getEvent().getId(),
                        item.getOffer().getEvent().getTitle()
                )).toList();
        //get cart
        return ResponseEntity.ok(new CartDetailsResponse(cart.getId(), items));
    }

    //merge after login
    @Operation(summary = "Fusionne le panier invité avec le compte utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Panier fusionné avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    @PutMapping("/merge")
    public ResponseEntity<CartResponse> mergeCart (
            @RequestBody MergeCartRequest request,
            Authentication authentication
    ) {
        //Load connected user
        User user = userService.findUserByEmail(authentication.getName());
        //Merge guest cart into user cart
        Cart merged = cartService.mergeCarts(request.sessionId(),  user);
        return ResponseEntity.ok(new CartResponse(merged.getId()));
    }


    //Dto
    public static record ValidateCartRequest(
            @Schema(description = "Identifiant du panier à valider")
            Long cartId
    ) {}
    public static record ValidateCartResponse(
            @Schema(description = "Statut de la validation")
            boolean ok,
            @Schema(description = "Montant total calculé")
            BigDecimal total,
            @Schema(description = "Liste des erreurs rencontrées")
            List<String> errors,
            @Schema(description = "Liste des QR hashes générés pour les billets")
            List<String> qrHashes
    ) {}

    public static record CartResponse(
            @Schema(description = "Identifiant du panier")
            Long cartId
    ){}

    public static record CartItemDetailsDto(
            @Schema(description = "ID de l'article")
            Long id,
            @Schema(description = "ID de l'offre")
            Long offerId,
            @Schema(description = "Quantité commandée")
            int quantity,
            @Schema(description = "ID de l'épreuve")
            Long eventId,
            @Schema(description = "Titre de l'épreuve")
            String eventTitle
    ) {}

    public static record CartDetailsResponse(
            @Schema(description = "Identifiant du panier")
            Long cartId,
            @Schema(description = "Détails des articles du panier")
            List<CartItemDetailsDto> items
    ) {
    }

    public static record MergeCartRequest(
            @Schema(description = "ID de session à fusionner")
            String sessionId
    ) {}

}
