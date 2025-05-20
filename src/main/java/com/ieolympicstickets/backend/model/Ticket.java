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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  (mock purchase key).
     */
    @Column(name = "purchase_key", nullable = false, unique = true)
    private String purchaseKey;

    /**
     * Clé finale encodée dans le QR (concaténation ou hash de accountKey + purchaseKey).
     */
    @Column(name = "qr_hash", nullable = false, unique = true)
    private String qrHash;

    /**
     * Owner
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Référence au panier (ou commande) validé.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /**
     * Référence à l'offre (type de billet) achetée.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    /**
     * Marque si le billet a déjà été utilisé/scanné.
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Date de création du billet.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
