package com.ieolympicstickets.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String qrHash) {
        super("Ticket not found with QR hash" + qrHash);
    }
}