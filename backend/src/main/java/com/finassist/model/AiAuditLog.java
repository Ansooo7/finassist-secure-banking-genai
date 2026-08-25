package com.finassist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 100, name = "correlation_id")
    private String correlationId;

    @Column(nullable = false, columnDefinition = "TEXT", name = "prompt_sanitized")
    private String promptSanitized;

    @Column(length = 50)
    private String intent;

    @Column(nullable = false, length = 30, name = "guardrail_status")
    private String guardrailStatus; // PASSED, INJECTION_BLOCKED, ADVICE_REFUSED

    @Column(columnDefinition = "TEXT", name = "guardrail_details")
    private String guardrailDetails;

    @Column(columnDefinition = "TEXT", name = "retrieved_sources")
    private String retrievedSources;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(length = 20, name = "response_status")
    private String responseStatus; // SUCCESS, REFUSED, ERROR

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private ZonedDateTime timestamp;
}
