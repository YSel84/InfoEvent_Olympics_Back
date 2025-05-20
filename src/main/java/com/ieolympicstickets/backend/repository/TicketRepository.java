package com.ieolympicstickets.backend.repository;

import com.ieolympicstickets.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
  //needed methods
}
