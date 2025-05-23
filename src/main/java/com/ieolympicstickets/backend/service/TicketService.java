package com.ieolympicstickets.backend.service;

import com.ieolympicstickets.backend.exceptions.TicketNotFoundException;
import com.ieolympicstickets.backend.model.Ticket;
import com.ieolympicstickets.backend.model.User;
import com.ieolympicstickets.backend.repository.TicketRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la gestion et la validation des billets.
 */
@Service
public class TicketService {

        private final TicketRepository ticketRepository;

        public TicketService(TicketRepository ticketRepository) {
                this.ticketRepository = ticketRepository;
        }

        /**
         * Récupère tous les tickets d'un utilisateur donné.
         * @param user utilisateur dont on veut les billets
         * @return liste des tickets
         */
        @Transactional(readOnly = true)
        public List<Ticket> findByUser(User user) {
                return ticketRepository.findByUser(user);
        }

        @Transactional(readOnly = true)
        public Ticket getByIdAndUser(Long ticketId, User user) {
                return ticketRepository.findById(ticketId)
                        .filter(t -> t.getUser().getId().equals(user.getId()))
                        .orElseThrow(() -> new AccessDeniedException("Ticket not yours"));
        }

        //QR scan
        @Transactional
        public boolean scanByHash(String qrHash) {
                Ticket ticket = ticketRepository.findByQrHash(qrHash)
                        .orElseThrow(() -> new TicketNotFoundException("QR invalide : " + qrHash));

                if (ticket.isUsed()) {
                        return false;
                }
                ticket.setUsed(true);
                ticketRepository.save(ticket);
                return true;
        }
}