package com.ieolympicstickets.backend.controller;

import com.ieolympicstickets.backend.model.Order;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.service.OrderService;
import com.ieolympicstickets.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Gestion des commandes")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @Operation(summary = "Liste les commandes de l'utilisateur connecté")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des commandes retournée"),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    @GetMapping
    public ResponseEntity<List<OrderDto>> listOrders(Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName());
        List<Order> orders = orderService.listUserOrders(user);
        List<OrderDto> dtos = orders.stream()
                .map(o -> new OrderDto(o.getId(), o.getTotal(), o.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Détail d'une commande")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Commande retournée"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée ou non autorisée")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailDto> getOrderDetail(
            @PathVariable Long orderId,
            Authentication authentication) {
        User user = userService.findUserByEmail(authentication.getName());
        Order order = orderService.getOrderByIdAndUser(orderId, user);
        List<OrderItemDto> items = order.getTickets().stream()
                .map(t -> new OrderItemDto(
                        t.getOffer().getOfferId(),
                        t.getOffer().getName(),
                        t.getQrHash(),
                        t.isUsed()
                )).collect(Collectors.toList());
        OrderDetailDto dto = new OrderDetailDto(
                order.getId(),
                order.getTotal(),
                order.getCreatedAt(),
                items
        );
        return ResponseEntity.ok(dto);
    }

    public static record OrderDto(
            Long orderId,
            BigDecimal total,
            LocalDateTime createdAt
    ) {}

    public static record OrderItemDto(
            Long offerId,
            String offerName,
            String qrHash,
            boolean used
    ) {}

    public static record OrderDetailDto(
            Long orderId,
            BigDecimal total,
            LocalDateTime createdAt,
            List<OrderItemDto> items
    ) {}
}
