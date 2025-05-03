package com.ieolympicstickets.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="offer_id", nullable = false)
    private Offer offer;

    @Column(name="quantity", nullable = false)
    private int quantity;

    @Column(name="updated_at",insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CartItem(){}

    //Convenience constructor
    public CartItem(Cart cart, Offer offer, int quantity) {
        this.cart = cart;
        this.offer = offer;
        this.quantity = quantity;
    }
}
