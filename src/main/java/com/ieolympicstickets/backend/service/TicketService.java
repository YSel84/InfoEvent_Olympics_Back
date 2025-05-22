package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.exceptions.TicketNotFoundException;
import com.ieolympicstickets.backend.model.Ticket;
import com.ieolympicstickets.backend.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la gestion et la validation des billets.
 */
@Service  // Création du bean Spring
public class TicketService {

        private final TicketRepository ticketRepository;

        public TicketService(TicketRepository ticketRepository) {
                this.ticketRepository = ticketRepository;
        }

        /**
         * Scans a ticket: marks it as used if not already.
         *
         * @param ticketId ID of the ticket to scan
         * @return true if successfully scanned, false if already used
         * @throws TicketNotFoundException if the ticket does not exist
         */
        @Transactional // Assure la gestion de la transaction
        public boolean scanTicket(Long ticketId) {
                Ticket ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new TicketNotFoundException(ticketId));

                if (ticket.isUsed()) {
                        return false;
                }
                ticket.setUsed(true);
                ticketRepository.save(ticket);
                return true;
        }
}