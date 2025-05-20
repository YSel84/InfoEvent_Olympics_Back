package com.ieolympicstickets.backend.service;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {
    @Value("${stripe.api-key}")
    private String stripeKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey=stripeKey;
    }

    /**
     * Create and confirm a PaymentIntent for said amount (cents)
     * return transaction Id
     *
     * */
    public String pay(int amountInCents, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long)amountInCents)
                .setCurrency(currency)
                .setConfirm(true)
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getId();
    }
}
