package com.finassist.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 20)
    private String sender; // USER or ASSISTANT

    @Column(nullable = false, columnDefinition = "TEXT", name = "message_text")
    private String messageText;

    @Column(length = 50, name = "intent_detected")
    private String intentDetected;

    @Column(length = 30, name = "guardrail_status")
    @Builder.Default
    private String guardrailStatus = "PASSED";

    @Column(columnDefinition = "TEXT", name = "data_points_summary")
    private String dataPointsSummary;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
