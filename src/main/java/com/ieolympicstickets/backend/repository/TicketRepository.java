package com.ieolympicstickets.backend.repository;

import com.ieolympicstickets.backend.model.Ticket;
import com.ieolympicstickets.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
  //needed methods

    List<Ticket> findByUser(User user);

    Optional<Ticket> findByQrHash(String qrHash);


    // Si besoin de trier par date de l'événement :
    // List<Ticket> findByUserOrderByOfferEventEventDateTime(User user);
}
