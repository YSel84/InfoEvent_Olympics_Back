package com.ieolympicstickets.backend.repository;

import com.ieolympicstickets.backend.model.Order;
import com.ieolympicstickets.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}

