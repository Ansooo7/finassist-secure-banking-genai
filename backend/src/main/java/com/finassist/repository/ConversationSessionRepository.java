package com.finassist.repository;

import com.finassist.model.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, UUID> {
    List<ConversationSession> findByCustomerIdOrderByStartedAtDesc(UUID customerId);
    Optional<ConversationSession> findByIdAndCustomerId(UUID sessionId, UUID customerId);
}
