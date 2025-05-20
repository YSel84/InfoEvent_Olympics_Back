package com.ieolympicstickets.backend.repository;

import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.CartItem;
import com.ieolympicstickets.backend.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    //find all items belonging to a cart
    List<CartItem> findByCart(Cart cart);

    //find a specific item in a cart
    Optional<CartItem> findByCartAndOffer(Cart cart, Offer offer);
}


