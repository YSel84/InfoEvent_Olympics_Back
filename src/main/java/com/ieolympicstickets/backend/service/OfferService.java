package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.model.Offer;
import com.ieolympicstickets.backend.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferService {
    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository=offerRepository;
    }
    public List<Offer> findAllOffers(){
        return offerRepository.findAll();
    }

    public List<Offer> findOffersByEvent(Long eventId) {
        return offerRepository.findByEventId(eventId);
    }

    public Offer saveOffer(Offer offer) {
     return offerRepository.save(offer);
    }
    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }


}
