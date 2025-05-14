package com.ieolympicstickets.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OfferNotFoundException extends RuntimeException{
    public OfferNotFoundException(Long offerId){
        super("Offre introuvable : " + offerId);
    }
    public OfferNotFoundException(String message){
        super(message);

    }
}
