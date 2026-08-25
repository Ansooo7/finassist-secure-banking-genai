package com.finassist.service;

import com.finassist.model.AiAuditLog;
import com.finassist.model.Customer;
import com.finassist.repository.AiAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AiAuditLogRepository auditLogRepository;

    public void logAiInteraction(
            Customer customer,
            String username,
            String correlationId,
            String promptSanitized,
            String intent,
            String guardrailStatus,
            String guardrailDetails,
            String retrievedSources,
            Integer latencyMs,
            String responseStatus
    ) {
        try {
            AiAuditLog auditLog = AiAuditLog.builder()
                    .customer(customer)
                    .username(username)
                    .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                    .promptSanitized(promptSanitized)
                    .intent(intent)
                    .guardrailStatus(guardrailStatus)
                    .guardrailDetails(guardrailDetails)
                    .retrievedSources(retrievedSources)
                    .latencyMs(latencyMs)
                    .responseStatus(responseStatus)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Recorded AI Audit Log: [Correlation: {}, Status: {}, Intent: {}]",
                    correlationId, guardrailStatus, intent);
        } catch (Exception e) {
            log.error("Failed to record AI audit log: {}", e.getMessage());
        }
    }
}
