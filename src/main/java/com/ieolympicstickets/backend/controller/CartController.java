package com.ieolympicstickets.backend.controller;


import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.service.CartService;
import com.ieolympicstickets.backend.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController (CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

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
            Long cartId
    ) {}
    public static record ValidateCartResponse(
            boolean ok,
            BigDecimal total,
            List<String> errors
    ) {}

    public static record CartResponse(Long cartId) {}

    public static record CartItemDetailsDto(
            Long id,
            Long offerId,
            int quantity,
           Long eventId,
            String eventTitle
    ) {}

    public static record CartDetailsResponse(
            Long cartId,
            List<CartItemDetailsDto> items
    ) {
    }

    public static record MergeCartRequest(
            String sessionId
    ) {}

}
