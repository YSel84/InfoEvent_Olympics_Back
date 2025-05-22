package com.ieolympicstickets.backend.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import com.ieolympicstickets.backend.exceptions.PaymentException;

@Service
public class PaymentService {
    /**
     * Simule un paiement pour un montant donné.
     * @param amount montant à payer en euros
     * @param token jeton de paiement (dummy pour mock)
     * @return true si le paiement est accepté
     * @throws PaymentException en cas d'erreur
     */
    public boolean pay(BigDecimal amount, String token) throws PaymentException {
        // Log pour mock
        System.out.println("[MOCK PAYMENT] amount=" + amount + " token=" + token);
        // Toujours accepté
        return true;
    }
}
