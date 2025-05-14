package com.ieolympicstickets.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(Long cartId) {
        super("Panier introuvable : " + cartId);
    }
    public CartNotFoundException(String message) {
        super(message);
    }
}
