package com.ieolympicstickets.backend.repository;


import com.ieolympicstickets.backend.model.Cart;
import com.ieolympicstickets.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository <Cart, Long> {
    //find connected user's cart
    Optional<Cart> findByUser(User user);

    List<Cart> findBySessionId(String sessionId);
}
