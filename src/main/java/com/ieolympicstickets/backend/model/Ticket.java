package com.ieolympicstickets.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Représente un billet généré lors de la validation du panier.
 * On y stocke la "purchaseKey" et le "qrHash" = concat(accountKey, purchaseKey).
 */
@Entity
@Table(name = "ticket")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_key", nullable = false, unique = true)
    private String purchaseKey;

    @Column(name = "qr_hash",     nullable = false, unique = true)
    private String qrHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}