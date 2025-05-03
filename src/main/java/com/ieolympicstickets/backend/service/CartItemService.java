package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.CartItem;
import com.ieolympicstickets.backend.model.Offer;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.repository.CartItemRepository;
import com.ieolympicstickets.backend.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ieolympicstickets.backend.exceptions.OfferNotFoundException;
import com.ieolympicstickets.backend.exceptions.CartNotFoundException;

@Service
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final OfferRepository offerRepository;

    public CartItemService(
            CartItemRepository cartItemRepository,
            CartService cartService,
            OfferRepository offerRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.offerRepository = offerRepository;
    }

    //add or increase quantity in a cart
    @Transactional
    public Cart addItem(String sessionId, User user, Long offerId, int quantity) {
        Cart cart = cartService.getOrCreateCart(sessionId, user);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(()-> new OfferNotFoundException(offerId));
        CartItem item = cartItemRepository
                .findByCartAndOffer(cart, offer)
                .orElseGet(() -> new CartItem(cart, offer, 0));
        item.setQuantity(item.getQuantity() + quantity);
        item.setCart(cart);
        item.setOffer(offer);
        cartItemRepository.save(item);
        return cart;
    }

    //update quantity of an existing item
    @Transactional
    public Cart updateItem(String sessionId, User user, Long itemId, int quantity) {
        Cart cart = cartService.getCart(sessionId, user)
                .orElseThrow(()-> new CartNotFoundException("Panier introuvable"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() ->new IllegalArgumentException("Item not found" + itemId));
        if(!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to this cart");
        }
        if(quantity<= 0){
            cartItemRepository.delete(item);
        }else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return cart;
    }

    //remove item from cart
    @Transactional
    public Cart removeItem(String sessionId, User user, Long itemId) {
        Cart cart = cartService.getCart(sessionId, user)
                .orElseThrow(() -> new CartNotFoundException("Panier introuvable"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(()-> new IllegalArgumentException("Item not found" + itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to this cart");
        }
        cartItemRepository.delete(item);
        return cart;
    }

}
