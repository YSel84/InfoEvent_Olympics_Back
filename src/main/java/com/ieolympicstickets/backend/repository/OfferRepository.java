package com.ieolympicstickets.backend.repository;

import com.ieolympicstickets.backend.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository <Offer, Long> {
    List<Offer> findByEventId(Long eventId);
}
