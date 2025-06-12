package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.model.Order;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.repository.OrderRepository;
import com.ieolympicstickets.backend.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    public OrderService(
            OrderRepository orderRepository,
            TicketRepository ticketRepository) {

        this.orderRepository = orderRepository;
        this.ticketRepository= ticketRepository;
    }

    @Transactional
    public Order createOrder(User user, BigDecimal total) {
        Order order = Order.builder()
                .user(user)
                .total(total)
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }

    public List<Order> listUserOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderByIdAndUser(Long orderId, User user) {
        return orderRepository.findById(orderId)
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new AccessDeniedException("Order not found or not yours"));
    }

       public long countItemsSoldByOffer(Long offerId) {
        return ticketRepository.countByOfferOfferId(offerId);
    }
}