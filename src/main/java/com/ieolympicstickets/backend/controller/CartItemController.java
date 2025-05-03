package com.ieolympicstickets.backend.controller;


import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.service.CartItemService;
import com.ieolympicstickets.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart/items")
public class CartItemController {
    private final CartItemService cartItemService;
    private final UserService userService;

    public CartItemController(
            CartItemService cartItemService,
            UserService userService
    ) {
        this.cartItemService = cartItemService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CartItemDetailsResponse> addItem(
            @RequestHeader(name="X-Session-Id", required = false) String sessionId,
            Authentication auth,
            @RequestBody AddCartItemRequest req
    ) {
        User user = (auth != null && auth.isAuthenticated())
                ? userService.findUserByEmail(auth.getName())
                : null;
        Cart cart = cartItemService.addItem(sessionId, user, req.offerId(), req.quantity());
        return ResponseEntity.ok(toDto(cart));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<CartItemDetailsResponse> updateItem(
            @RequestHeader(name="X-Session-Id", required = false) String sessionId,
            Authentication auth,
            @PathVariable Long itemId,
            @RequestBody UpdateCartItemRequest req
    ) {
        User user = (auth != null && auth.isAuthenticated())
                ? userService.findUserByEmail(auth.getName())
                : null;
        Cart cart = cartItemService.updateItem(sessionId, user, itemId, req.quantity());
        return ResponseEntity.ok(toDto(cart));
    }

    @DeleteMapping("/{idemId}")
    public ResponseEntity<CartItemDetailsResponse> deleteItem(
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId,
            Authentication auth,
            @PathVariable Long idemId
    ) {
        User user = (auth != null && auth.isAuthenticated())
                ? userService.findUserByEmail(auth.getName())
                : null;
        Cart cart = cartItemService.removeItem(sessionId, user, idemId);
        return ResponseEntity.ok(toDto(cart));
    }


    //helper
    private CartItemDetailsResponse toDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(i -> {
                    var offer = i.getOffer();
                    var event = offer.getEvent();
                    return new CartItemDto(
                            offer.getOfferId(),
                            i.getQuantity(),
                            event.getId(),
                            event.getTitle()
                    );
                })
                .collect(Collectors.toList());
        return new CartItemDetailsResponse(cart.getId(), items);
    }

    // DTOs
    public static record CartItemDto(Long offerId, int quantity, Long eventId, String eventTitle) {}
    public static record CartItemDetailsResponse(Long cartId, List<CartItemDto> items) {}
    public static record AddCartItemRequest(Long offerId, int quantity) {}
    public static record UpdateCartItemRequest(int quantity) {}

}
