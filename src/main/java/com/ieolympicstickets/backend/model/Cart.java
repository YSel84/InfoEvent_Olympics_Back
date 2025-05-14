package com.ieolympicstickets.backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="cart")
public class Cart {

    public Cart(){
    }

    public Cart (String sessionId, User user){
        this.sessionId = sessionId;
        this.user = user;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    /**
     * sessionId for non-authed user;
     * User connected -> cart to user
     **/
    @Column(name="session_id", nullable = true, unique = true)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Cart items
     * */
    @OneToMany(mappedBy = "cart", cascade= CascadeType.ALL, orphanRemoval = true)
    private List <CartItem> items = new ArrayList<>();




    //Convenience
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}
