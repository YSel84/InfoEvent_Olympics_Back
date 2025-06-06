package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.controller.CartController.ValidateCartResponse;
import com.ieolympicstickets.backend.exceptions.PaymentException;
import com.ieolympicstickets.backend.model.*;
import com.ieolympicstickets.backend.repository.CartRepository;
import com.ieolympicstickets.backend.repository.OfferRepository;
import com.ieolympicstickets.backend.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ieolympicstickets.backend.service.PaymentService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class CartService {

    private final CartRepository cartRepository;
    private final OfferRepository offerRepository;
    //private final StripeService stripeService;
    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;
    private final OrderService orderService;

    public CartService(
            CartRepository cartRepository,
            OfferRepository offerRepository,
            //StripeService stripeService,
            TicketRepository ticketRepository,
            PaymentService paymentService,
            OrderService orderService

   ) {
        this.cartRepository = cartRepository;
        this.offerRepository = offerRepository;
        this.ticketRepository = ticketRepository;
        this.paymentService  = paymentService;
        this.orderService = orderService;
    }

    @Transactional
    public ValidateCartResponse validateCart(Long cartId, User user, String paymentToken) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Panier introuvable : " + cartId));

        if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
            return new ValidateCartResponse(
                    false,
                    BigDecimal.ZERO,
                    List.of("Vous n'êtes pas autorisé à valider ce panier."),
                    List.of()
            );
        }

        BigDecimal total = BigDecimal.ZERO;
        List<String> errors = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Offer offer = offerRepository.findById(item.getOffer().getOfferId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Offre introuvable pour l'item " + item.getId()
                    ));
            int qty = item.getQuantity();
            if (offer.getStock() < qty) {
                errors.add(String.format(
                        "Rupture de stock pour '%s' : demandé %d, disponible %d",
                        offer.getName(), qty, offer.getStock()
                ));
            }
            total = total.add(offer.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        if (!errors.isEmpty()) {
            return new ValidateCartResponse(false, total, errors, List.of());
        }


        /**
        // Simulate payment via Stripe mock
        try {
            stripeService.pay(
                    total.multiply(BigDecimal.valueOf(100)).intValue(),
                    "eur"
            );
        } catch (StripeException e) {
            errors.add("Erreur de paiement : " + e.getMessage());
            return new ValidateCartResponse(false, total, errors, List.of());
        }*/

        //Mock payment service
        try {
            paymentService.pay(total, paymentToken);
        } catch (PaymentException e) {
            errors.add("Erreur de paiement : " + e.getMessage());
            return new ValidateCartResponse(false, total, errors, List.of(e.getMessage()));
        }

        //Create an order
        Order order = orderService.createOrder(user, total);

        // Generate tickets and QR hashes
        String accountKey = user.getUserKey();
        List<String> qrHashes = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Offer offer = offerRepository.findById(item.getOffer().getOfferId())
                    .orElseThrow();
            for (int i = 0; i < item.getQuantity(); i++) {
                String purchaseKey = UUID.randomUUID().toString();
                String qrHash = accountKey + purchaseKey;
                Ticket ticket = Ticket.builder()
                        .user(user)
                        .order(order)
                        .offer(offer)
                        .purchaseKey(purchaseKey)
                        .qrHash(qrHash)
                        .used(false)
                        .build();
                ticketRepository.save(ticket);
                qrHashes.add(qrHash);
            }
        }
        //empty cart after purchase
        cart.getItems().clear();
        cartRepository.save(cart);

        return new ValidateCartResponse(true, total, errors, qrHashes);
    }

    @Transactional
    public Cart getOrCreateCart(String sessionId, User user) {
        if (user != null) {
            // Try existing cart for existing user
            Optional<Cart> optUserCart = cartRepository.findByUser(user);
            if (optUserCart.isPresent()) {
                return optUserCart.get();
            }

            //merge a guest cart (même sessionId)
            if (sessionId != null) {
                List<Cart> guestCarts = cartRepository.findBySessionId(sessionId);
                if (!guestCarts.isEmpty()) {
                    Cart guestCart = guestCarts.get(0);
                    guestCart.setUser(user);
                    return cartRepository.save(guestCart);
                }
            }

            // Create a new cart
            Cart newCart = new Cart(sessionId, user);
            return cartRepository.save(newCart);

        } else {
            // invité / guest : on cherche par sessionId ou on crée
            List<Cart> sessionCarts = cartRepository.findBySessionId(sessionId);
            if (!sessionCarts.isEmpty()) {
                return sessionCarts.get(0);
            }
            Cart newCart = new Cart(sessionId, null);
            return cartRepository.save(newCart);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Cart> getCart(String sessionId, User user) {
        if (user != null) {
            // Optional directement
            return cartRepository.findByUser(user);
        }
        // guest : on peut avoir plusieurs paniers, on prend le premier
        return cartRepository.findBySessionId(sessionId)
                .stream()
                .findFirst();
    }


    @Transactional
    public Cart mergeCarts(String sessionId, User user) {
        // Récupère le panier invité (s’il existe)
        Optional<Cart> guestOpt = cartRepository.findBySessionId(sessionId).stream().findFirst();
        // Récupère (ou crée/adopte) le panier utilisateur
        Cart userCart = getOrCreateCart(sessionId, user);

        if (guestOpt.isEmpty() || guestOpt.get().getId().equals(userCart.getId())) {
            // pas de fusion à faire
            return userCart;
        }

        Cart guestCart = guestOpt.get();
        // --- fusion des items ---
        for (CartItem gi : guestCart.getItems()) {
            gi.setCart(null);             // détache le guestItem
            Optional<CartItem> existing =
                    userCart.getItems().stream()
                            .filter(ui -> ui.getOffer().getOfferId().equals(gi.getOffer().getOfferId()))
                            .findFirst();

            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity() + gi.getQuantity());
            } else {
                CartItem copy = new CartItem();
                copy.setOffer(gi.getOffer());
                copy.setQuantity(gi.getQuantity());
                userCart.addItem(copy);
            }
        }

        // persister puis supprimer l’ancien panier invité
        userCart = cartRepository.save(userCart);
        cartRepository.delete(guestCart);
        return userCart;
    }
}
