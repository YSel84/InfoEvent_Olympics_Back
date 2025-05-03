package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.controller.CartController.ValidateCartResponse;
import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.CartItem;
import com.ieolympicstickets.backend.model.Offer;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.repository.CartRepository;
import com.ieolympicstickets.backend.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CartService {

    private final CartRepository cartRepository;
    private final OfferRepository offerRepository;

    public CartService(CartRepository cartRepository, OfferRepository offerRepository) {
        this.cartRepository = cartRepository;
        this.offerRepository = offerRepository;
    }

    @Transactional(readOnly = true)
    public ValidateCartResponse validateCart(Long cartId, User user) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Panier introuvable : " + cartId));

        if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
            return new ValidateCartResponse(
                    false,
                    BigDecimal.ZERO,
                    List.of("Vous n'êtes pas autorisé à valider ce panier.")
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

        return new ValidateCartResponse(errors.isEmpty(), total, errors);
    }

    @Transactional
    public Cart getOrCreateCart(String sessionId, User user) {
        if (user != null) {
            // --- 1) tenter le panier existant de l’utilisateur ---
            Optional<Cart> optUserCart = cartRepository.findByUser(user);
            if (optUserCart.isPresent()) {
                return optUserCart.get();
            }

            // --- 2) adopter un panier invité existant (même sessionId) ---
            if (sessionId != null) {
                List<Cart> guestCarts = cartRepository.findBySessionId(sessionId);
                if (!guestCarts.isEmpty()) {
                    Cart guestCart = guestCarts.get(0);
                    guestCart.setUser(user);
                    return cartRepository.save(guestCart);
                }
            }

            // --- 3) enfin, vraiment créer un nouveau panier pour cet user ---
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



    /**
    //@Transactional
    public Cart getOrCreateCart(String sessionId, User user) {
        // si user connecté, on cherche par user ; sinon par sessionId
        List<Cart> existing = (user != null)
                ? cartRepository.findByUser(user).map(List::of).orElse(List.of())
                : cartRepository.findBySessionId(sessionId);

        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        // pas de panier existant → on en crée un
        Cart cart = new Cart(sessionId, user);
        return cartRepository.save(cart);
    }*/

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

        // --- persister puis supprimer l’ancien panier invité ---
        userCart = cartRepository.save(userCart);
        cartRepository.delete(guestCart);
        return userCart;
    }


/**
    //@Transactional
    public Cart mergeCarts(String sessionId, User user) {
        // 1) on récupère (ou crée) le panier de l'user, en lui passant bien le sessionId courant
        Cart userCart = getOrCreateCart(sessionId, user);

        // 2) on récupère le panier guest s'il existe
        Optional<Cart> guestOpt = getCart(sessionId, null);
        if (guestOpt.isPresent()) {
            Cart guestCart = guestOpt.get();

            // si ce n'est pas déjà le même panier
            if (!guestCart.getId().equals(userCart.getId())) {
                // 3) pour chaque item du guest, on cherche un équivalent dans userCart
                for (CartItem guestItem : guestCart.getItems()) {
                    Offer offer = guestItem.getOffer();
                    int qty = guestItem.getQuantity();

                    // a) si l'offre existe déjà dans userCart, on incrémente
                    CartItem existing = userCart.getItems().stream()
                            .filter(ci -> ci.getOffer().getOfferId().equals(offer.getOfferId()))
                            .findFirst()
                            .orElse(null);

                    if (existing != null) {
                        existing.setQuantity(existing.getQuantity() + qty);

                        // b) sinon on crée un NOUVEL item (jamais ré-utiliser guestItem)
                    } else {
                        CartItem copy = new CartItem();
                        copy.setOffer(offer);
                        copy.setQuantity(qty);
                        userCart.addItem(copy);
                    }
                }

                // 4) on sauvegarde d'abord userCart pour persister les nouveaux items
                userCart = cartRepository.save(userCart);

                // 5) puis on supprime proprement le panier guest
                cartRepository.delete(guestCart);
            }
        }
        return userCart;
    }*/

}
